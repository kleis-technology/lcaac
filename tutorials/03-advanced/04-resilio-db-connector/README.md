# Resilio DB connector

## Description

So far, the datasources have been embodied as csv files.
In this tutorial, we explain how fetch the impacts of IT equipments
using the ResilioDB connector.

Open `main.lca`. Notice that Resilio DB connector expects
the unit 'GB' to have been defined. This unit is not part of the built-in units,
so we have to define it.

Next we define two datasources, `server_impacts` and `switch_impacts`.
In the past tutorials, these datasources were connected to csv files.

Open `lcaac.yaml` to see how these connections are described.
The block `connectors` configures the connectors `csv` and `resilio_db`.
For `resilio_db`, you fill in the api url `https://db.resilio.tech`,
and your access token. The access token can also be provided via
the environment variable `RESILIO_DB_ACCESS_TOKEN`.

Focus now on the `server` part in the block `datasources`.
The part
```yaml
  - name: server_params
    connector: csv
    location: server_params.csv
```
indicates that there is a datasource `server_params` connected to the csv file `server_params.csv`.
The part
```yaml
  - name: server_impacts
    connector: resilio_db
    options:
      paramsFrom: server_params
      endpoint: rack_server
```
states that the datasource `server_impacts` is to be fed with data from resilio db.
For each request of an impact for, e.g., `id = srv-01`,
the request parameters are read from the datasource `server_params`, 
as indicated by the option `paramsFrom`.
Moreover, the connector should call the endpoint `rack_server`.
Notice that the data source `server_params` is expected to 
have a specific schema.

## Commands

You can provide your Resilio DB access token directly in `lcaac.yaml`,
or using an environment variable.
```bash
export RESILIO_DB_ACCESS_TOKEN=your-access-token-here
```

Then, you can assess or trace as usual
```bash
lcaac assess server
lcaac assess switch
lcaac trace server
lcaac trace switch
```

## Supported endpoints and format

For now, the supported endpoints are:
- rack_server
- switch

For the endpoint `rack_server`, `server_params` must provide:
- `model_name`: string
- `rack_unit`: integer
- `cpu_name`: string
- `cpu_quantity`: integer
- `ram_total_size_gb`: double
- `ssd_total_size_gb`: double

For the endpoint `switch`, `switch_params` must provide:
- `cpu_name`: string
- `cpu_quantity`: integer
- `ram_total_size_gb`: double

