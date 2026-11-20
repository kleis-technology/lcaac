# Serving a model as an HTTP API

`lcaac serve` runs an HTTP server whose API is declared in the project file `lcaac.yaml`, under the key
`api`. Each operation of the API evaluates a process of the model with the values of a request, and
returns parts of the result. Clients get a stable contract, while the model can still evolve behind it.

## Try it yourself

The file `main.lca` models a fleet of virtual machines: each VM consumes electricity, whose impacts
depend on the grid of its region. The inventory of VMs and the grid mixes come from the CSV files in
`data/`.

Start the server:

```bash
cd tutorials/03-advanced/06-serve
lcaac serve
```

```
Serving cloud-assess 1.0.0 on http://127.0.0.1:8080
```

Use `--host` and `--port` to listen elsewhere. The server stops with Ctrl-C.

## The API definition

The project file `lcaac.yaml` names the API and declares its operations, under the key `api`:

```yaml
name: serve
description: Sample project to illustrate lcaac serve

api:
  name: cloud-assess
  version: 1.0.0
  description: Impacts of cloud resources
  units:
    impacts: { GWP: kg_CO2_Eq }
    products: { vm_hour: hour, electricity: kWh }
  operations:
    virtual_machine:
      description: Impacts of one hour of a virtual machine
      process: virtual_machine
      inputs:
        vcpu: { parameter: vcpu, description: Number of virtual CPUs }
        region: { parameter: region }
      outputs:
        impacts:
          select: [GWP]
          where: { product: vm_hour, process: virtual_machine }
        electricity:
          select: [amount]
          where: { product: electricity, process: grid }

    vm_fleet:
      description: Impacts of a fleet of virtual machines
      process: vm_fleet
      data:
        vms: { datasource: vm_inventory, description: The virtual machines of the fleet }
      outputs:
        total:
          select: [GWP]
          where: { product: fleet, process: vm_fleet }
          aggregate: one
        per_vm:
          select: [arguments, amount, GWP]
          where: { product: vm_hour, process: virtual_machine }
        electricity:
          select: [arguments, amount, GWP]
          where: { product: electricity, process: grid }
```

Each operation is served at `POST /<operation>`. It evaluates `process`, for one unit of its reference
product, as `lcaac assess` does.

| Field | Meaning |
|---|---|
| `process` | the process to evaluate |
| `labels` | labels fixed by the API, to select a variant of the process, e.g. `labels: { model: standard }` |
| `inputs` | the fields of the request that give a value to a parameter (`{ parameter: name }`) or to a label (`{ label: name }`) of the process. The name of a field may differ from the name in the model. |
| `data` | the fields of the request that give the rows of a data source (`{ datasource: name }`), instead of its own rows |
| `outputs` | the outputs of the response (see below) |

`units`, for the whole API, gives the units of the quantities of the responses (see below).

Parameters that are not given in the request keep their default values, and the API cannot set the
other parameters of the model.

### Outputs select cells of the trace

The outputs come from the table of `lcaac trace`, the most detailed result of an evaluation:

```bash
lcaac trace vm_fleet
```

```
depth  name         a                c                                   amount  unit  GWP   GWP_unit
-----  -----------  ---------------  ----------------------------------  ------  ----  ----  ---------
0      fleet        vm_fleet         {}                                  1       p     8.64  kg CO2-Eq
1      vm_hour      virtual_machine  {id=vm-001, vcpu=4.0 u, region=FR}  720     hour  1.44  kg CO2-Eq
1      vm_hour      virtual_machine  {id=vm-002, vcpu=2.0 u, region=DE}  720     hour  5.76  kg CO2-Eq
1      vm_hour      virtual_machine  {id=vm-003, vcpu=8.0 u, region=FR}  360     hour  1.44  kg CO2-Eq
2      electricity  grid             {region=DE}                         14.4    kWh   5.76  kg CO2-Eq
2      electricity  grid             {region=FR}                         57.6    kWh   2.88  kg CO2-Eq
```

(Some columns are left out, and amounts are rounded.) Each row is a product of a process of the supply
chain: `amount` is how much of it the evaluated process needs, and each indicator is the impact embodied
in that amount, everything upstream included.

An output picks rows with `where`, and columns with `select`:

- `where` gives conditions on the rows: `product`, `process`, `labels` and `arguments` (string arguments
  only). Each condition narrows the selection, and at least one is required.
- `select` lists the columns: `product`, `process`, `labels`, `arguments`, `amount`, or the name of an
  indicator. `amount` is the amount of one product, so it requires `where.product`.

A label or an argument takes a value, a list of values, or `null` for a row without it:

```yaml
where: { process: vm_fn, labels: { phase: embodied } }           # the rows with phase = embodied
where: { process: vm_fn, labels: { phase: [embodied, use] } }    # with phase = embodied or use
where: { process: vm_fn, labels: { phase: null } }               # without the label phase
```

This tells apart rows that share their product and process, e.g. a process that calls variants of itself
for each step of its life cycle, with a label `phase`: `phase: null` selects the process itself, and
`phase: [embodied, use]` its steps.

By default, an output is a list, with one element per matching row, in the order of the trace. It is
empty when no row matches. With `aggregate`, it is a single element:

- `aggregate: one`: the only matching row. The request fails if not exactly one row matches.
- `aggregate: sum`: the sum of the matching rows, for the indicators and `amount` only. Since each row
  includes everything upstream, the request fails if a matching row is upstream of another one: it would
  be counted twice. With no matching row, the sum is 0.

### Units

The quantities of the responses are in the units declared under `units`, whatever the units of the
model: `impacts` for the indicators (and substances), `products` for the column `amount` of each product.
The values of the model are converted, so that the model can change its units without changing the
contract. Every column of quantities that an output selects needs a unit, in the syntax of the language
(`kg_CO2_Eq`, `kW * year`): the units of the prelude, or those that the model declares.

An indicator that the evaluated system does not contain is 0: it has no contribution.

The dimensions of the units cannot be checked at startup. A unit of another dimension than the values
of the model, e.g. `kWh` for `GWP`, fails the requests with a 500 error, since the API does not fit the
model.

To get one row per VM, the model passes the id of each VM to `virtual_machine`
(`from virtual_machine(id = vm.id, ...)`). Otherwise, two VMs with the same parameters would be the same
process, and the same row of the trace. Making items visible is a choice of the model: the API returns
what the trace shows, nothing more.

### Checked at startup

The definition is checked against the model before the server starts. Unknown fields are errors, as
well as names that the model does not define. With a misspelled field, an unknown data source and an
unknown product, the server does not start, and says where the errors are:

```
./lcaac.yaml:15:7: api.operations.virtual_machine.ouputs: Unknown property 'ouputs'. Known properties are: data, description, inputs, labels, outputs, process
./lcaac.yaml:27:28: api.operations.vm_fleet.data.vms.datasource: unknown data source 'vm_list'
./lcaac.yaml:31:29: api.operations.vm_fleet.outputs.total.where.product: unknown product 'fleets'
```

## Calling the API

`GET /` describes the API:

```bash
curl localhost:8080/
```

```json
{
  "name": "cloud-assess",
  "version": "1.0.0",
  "description": "Impacts of cloud resources",
  "operations": ["virtual_machine", "vm_fleet"]
}
```

### Parameters

```bash
curl -X POST localhost:8080/virtual_machine -H 'Content-Type: application/json' \
  -d '{"vcpu": 4, "region": "DE"}'
```

```json
{
  "request": { "vcpu": 4, "region": "DE" },
  "outputs": {
    "impacts": [
      { "GWP": { "amount": 0.016, "unit": "kg CO2-Eq" } }
    ],
    "electricity": [
      { "amount": { "amount": 0.04, "unit": "kWh" } }
    ]
  }
}
```

A response repeats the request, as it was sent, and gives the outputs of the operation. Each response
is self-contained: it can be stored or compared without the request it answers.

A quantity can be given in three ways:

- text in the language syntax: `"4 u"`, `"100 hour"`;
- a number, in the unit of the default value: `4`;
- an amount and a unit: `{"amount": 720, "unit": "hour"}`.

In responses, quantities are always an amount and a unit, the unit of the API.

### Rows of a data source

Without `vms`, the operation uses the rows of `data/vm_inventory.csv`:

```bash
curl -X POST localhost:8080/vm_fleet
```

```json
{
  "request": {},
  "outputs": {
    "total": { "GWP": { "amount": 8.64, "unit": "kg CO2-Eq" } },
    "per_vm": [
      {
        "arguments": { "id": "vm-001", "vcpu": "4.0 u", "region": "FR" },
        "amount": { "amount": 720, "unit": "hour" },
        "GWP": { "amount": 1.44, "unit": "kg CO2-Eq" }
      },
      ...
    ],
    "electricity": [
      {
        "arguments": { "region": "DE" },
        "amount": { "amount": 14.4, "unit": "kWh" },
        "GWP": { "amount": 5.76, "unit": "kg CO2-Eq" }
      },
      ...
    ]
  }
}
```

(Amounts are rounded here.) With `vms`, the rows of the request replace the rows of the data source, for
this request only. A column that is not given takes the default value of the schema of the data source,
e.g. the region `FR` for `web-1`:

```bash
curl -X POST localhost:8080/vm_fleet -H 'Content-Type: application/json' -d '{"vms": [
  {"id": "web-1", "vcpu": "4 u", "hours": "100 hour"},
  {"id": "db-1", "vcpu": 16, "hours": {"amount": 720, "unit": "hour"}, "region": "DE"}
]}'
```

```json
{
  "request": {
    "vms": [
      { "id": "web-1", "vcpu": "4 u", "hours": "100 hour" },
      { "id": "db-1", "vcpu": 16, "hours": { "amount": 720, "unit": "hour" }, "region": "DE" }
    ]
  },
  "outputs": {
    "total": { "GWP": { "amount": 46.28, "unit": "kg CO2-Eq" } },
    "per_vm": [
      {
        "arguments": { "id": "db-1", "vcpu": "16.0 u", "region": "DE" },
        "amount": { "amount": 720, "unit": "hour" },
        "GWP": { "amount": 46.08, "unit": "kg CO2-Eq" }
      },
      {
        "arguments": { "id": "web-1", "vcpu": "4.0 u", "region": "FR" },
        "amount": { "amount": 100, "unit": "hour" },
        "GWP": { "amount": 0.2, "unit": "kg CO2-Eq" }
      }
    ],
    "electricity": [ ... ]
  }
}
```

The request is repeated as it was sent, rows included: the values actually used, with their defaults,
are in `arguments`.

The rows of `per_vm` are in the order of the trace, not of the request: use `arguments` to tell them
apart. The total of the fleet is the sum of the VMs (46.08 + 0.2), and also the sum of the electricity,
since each row includes everything upstream. It is not the sum of both: with `aggregate: sum`, an output
with `where: { arguments: { region: FR } }`, which matches the VMs in France and their electricity, is
refused.

### Several requests at once

Each operation is also served at `POST /<operation>/batch`, to run several requests in one call, as
`lcaac assess -f` does with the rows of a CSV file. The body lists the requests, each one as it would be
sent alone, and the response gives their results in the same order. Each result is the response of the
request alone, which it repeats:

```bash
curl -X POST localhost:8080/virtual_machine/batch -H 'Content-Type: application/json' \
  -d '{"requests": [{"vcpu": 4, "region": "DE"}, {"vcpu": 2}]}'
```

```json
{
  "results": [
    {
      "request": { "vcpu": 4, "region": "DE" },
      "outputs": {
        "impacts": [ { "GWP": { "amount": 0.016, "unit": "kg CO2-Eq" } } ],
        "electricity": [ { "amount": { "amount": 0.04, "unit": "kWh" } } ]
      }
    },
    {
      "request": { "vcpu": 2 },
      "outputs": {
        "impacts": [ { "GWP": { "amount": 0.001, "unit": "kg CO2-Eq" } } ],
        "electricity": [ { "amount": { "amount": 0.02, "unit": "kWh" } } ]
      }
    }
  ]
}
```

The requests are assessed one after the other, with the same evaluator: a process annotated with
`@cached` is evaluated once for the whole batch, instead of once per request. A request that gives rows
of a data source is the exception: a cached process may read these rows, so it is evaluated on its own.

To assess many items, a batch of small requests is much faster than one request with many rows: each
request is a small system. With this model, a batch of 100,000 VMs takes a few seconds.

A batch fails as a whole when one of its requests fails, and the error names the request:

```json
{
  "error": "incompatible dimensions: expected none, found mass",
  "frames": [
    { "description": "argument 'vcpu' = 2 kg" },
    { "description": "request 'requests[1]'" }
  ]
}
```

A batch holds at most 10,000 requests, unless the server is started with e.g. `--max-batch 100000`.

### Errors

A request that cannot be read is a `400` error: an unknown field, or a value that is not a quantity or
has the wrong dimension.

```bash
curl -X POST localhost:8080/vm_fleet -d '{"vms": [{"id": "web-1", "hours": "100 kg"}]}'
```

```json
{
  "error": "incompatible dimensions: expected time, found mass",
  "frames": [ { "description": "argument 'vms[0].hours' = 100 kg" } ]
}
```

An evaluation that fails is a `422` error, with the same frames as `lcaac assess` gives, and their
locations in the model:

```bash
curl -X POST localhost:8080/virtual_machine -d '{"region": "CH"}'
```

```json
{
  "error": "no record found in datasource 'grid_mix' [data/grid_mix.csv] matching {region=CH}",
  "frames": [
    { "description": "variable 'mix'", "location": "./main.lca:50:9" },
    { "description": "impact 'GWP'", "location": "./main.lca:56:9" },
    { "description": "process 'grid'", "location": "./main.lca:45:1" },
    { "description": "supplier of 'electricity' for process 'virtual_machine'", "location": "./main.lca:41:9" }
  ]
}
```

The locations help the maintainers of the API, but they show the structure of the model to clients.
`lcaac serve --hide-locations` leaves them out.

One failure fails the whole request: there are no partial results.

## The OpenAPI document

The contract of the API is also given as an OpenAPI document, served at `GET /openapi.json`. It can be
generated without a server, e.g. to publish it, review its changes, or generate clients:

```bash
lcaac api openapi > openapi.json
```

The document is derived from the definition and the model: the types, units and default values of the
fields come from the parameters and data sources of the model, and the values of a label are listed. The
unit of each quantity of a response is the unit of the API.
It describes the contract only: the names of the processes, parameters and data sources behind it are
not in it, so that the model can change without changing the contract.
