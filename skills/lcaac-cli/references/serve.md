# lcaac serve

`lcaac serve` serves an HTTP API declared under the key `api` of `lcaac.yaml`. The definition is checked against the
model at startup: if it refers to anything that the model does not define, the server does not start, and the errors
are located in `lcaac.yaml`. `lcaac api openapi` prints the OpenAPI document of the API without serving it.

Options: `--host` (default `127.0.0.1`), `-p/--port` (default `8080`), `--max-batch` (requests per batch, default
10000), `--hide-locations` (no locations of the model in the errors sent to clients), and `-c`, `-s` as for the other
commands.

## Example

```yaml
api:
  name: cloud-assess
  version: 1.0.0
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
          aggregate: one
        electricity:
          select: [amount]
          where: { product: electricity }
```

```
$ curl -X POST localhost:8080/virtual_machine -d '{"vcpu": 4, "region": "DE"}'
{"request": {"vcpu": 4, "region": "DE"},
 "outputs": {"impacts": {"GWP": {"amount": 0.016, "unit": "kg CO2-Eq"}},
             "electricity": [{"amount": {"amount": 0.04, "unit": "kWh"}}]}}
```

## The definition

`api`: `name`, `version`, `description`, `units`, `operations`. Each operation is served at `POST /<operation>`:

| Field | Meaning |
|---|---|
| `process` | the process to evaluate, for one unit of its reference product |
| `labels` | labels fixed by the API, to select a variant: `labels: { model: standard }` |
| `inputs` | request fields, each mapped to exactly one of `{ parameter: name }`, `{ label: name }` (of the process) or `{ global: name }` (a global parameter of the model, for this request only, as `-G`). Optional `description` |
| `data` | request fields whose rows replace a data source: `{ datasource: name }`, optional `columns` and `pass_through` |
| `for_each` | `{ rows: <data field>, arguments: { <parameter>: <column> } }`: one evaluation per row, one result per row |
| `pass_through` | request fields accepted with any value, never read, repeated in the response |
| `outputs` | the outputs of the response, at least one |

Parameters, labels and globals that a request does not give keep their values from the model. The API cannot set
anything else.

### Outputs

An output selects cells of the trace table (`lcaac trace`): rows with `where`, columns with `select`.

- `where` (at least one condition): `product`, `process`, `labels` and `arguments` (string arguments only). A label
  or argument takes a value, a list of values (any of them), or `null` (rows without it):
  `labels: { phase: null }`, `labels: { phase: [embodied, use] }`.
- `select`: `product`, `process`, `labels`, `arguments`, `amount`, or the name of an indicator or substance.
  `amount` requires `where.product`.
- By default an output is a list, one element per matching row. `aggregate: one` gives the only matching row (an
  error if not exactly one matches); `aggregate: sum` the sum of the matching rows, for indicators and `amount` only,
  0 when no row matches, and an error when a matching row is upstream of another one (counted twice).
- Every row includes everything upstream of it: to tell apart a process and the steps it calls (same product and
  process name), use conditions on labels or arguments, e.g. `phase: null` for the process itself.

### Units

`units.impacts` (indicators and substances) and `units.products` (the `amount` of each product) are required for
every selected column of quantities, in the syntax of the language (`kg_CO2_Eq`, `kW * year`). Results are converted
into them, and an indicator absent from the evaluated system is 0. A unit of another dimension than the model fails
the requests with a 500.

### Data rows

- A request field of `data` is a list of rows (JSON objects); they replace the rows of the data source, for this
  request only. Without the field, the rows of the model are used. A column not given takes the default value of the
  schema.
- Without `columns`, a row gives any column of the data source, under its own name. With `columns`
  (`{ <request name>: <column> }`), exactly these columns, under the names of the request.
- `pass_through: [meta]` on a data field: fields of the rows accepted and repeated, never read.
- With `for_each`, the rows replace the data source once for all of them and share one evaluator: a process can look
  up its row (`lookup ... match id = id`) or sum over all the rows, and `@cached` processes are evaluated once.
  The response is `{"request": <without the rows>, "results": [{"row": <as sent>, "outputs": {...}}]}`.

## Requests and responses

- Body: a JSON object (an empty body is `{}`). Unknown fields are errors.
- Quantities: text in the language syntax (`"4 u"`, `"100 hour"`), a number in the unit of the default value, or
  `{"amount": 720, "unit": "hour"}`. Their dimension must be that of the default value.
- Response: `{"request": <as sent>, "outputs": {...}}`; quantities are `{"amount": ..., "unit": ...}`.
- `POST /<operation>/batch` with `{"requests": [...]}` gives `{"results": [...]}`, in order, each result being that of
  the request alone. Requests are evaluated one after the other with one evaluator (per value of the globals given);
  one failure fails the batch, naming `requests[i]`.
- Errors: `{"error": ..., "frames": [{"description": ..., "location": ...}], "related": [...]}`, with status 400 (the
  request cannot be read), 404 (unknown operation), 422 (the evaluation fails) or 500 (the API does not fit the model).
- `GET /` (name, version, operations), `GET /health` (`{"status": "UP"}`), `GET /openapi.json`.

## OpenAPI

The document describes the contract only: types and units of the fields, values of labels, outputs and their units.
No names of the model (processes, parameters, data sources), and no default values (the model may compute them).
