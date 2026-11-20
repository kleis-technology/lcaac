# lcaac.yaml

The configuration of a project. It is optional: without it, CSV data is read relative to the current directory.
Commands read `./lcaac.yaml`, or the file given with `-c`; its directory is the project directory.

```yaml
name: my-project
description: Impacts of cloud resources

connectors:
  - name: csv
    options:
      directory: data          # CSV files of the data sources, relative to the project directory
    cache:
      enabled: true            # keep the rows read in memory (useful for lookups in large files)
      maxSize: 1024
      maxRecordsPerCacheLine: 8192

api:                           # optional: the API served by `lcaac serve`, see serve.md
  ...
```

| Key | Meaning |
|---|---|
| `name`, `description` | Describe the project |
| `connectors` | Where the rows of data sources come from. One connector exists: `csv`, with the option `directory` (default `.`, the project directory) and an optional `cache` |
| `api` | The API of `lcaac serve` |

Unknown top-level keys are ignored, so that a key starting with `x-` can hold YAML anchors to reuse elsewhere
(e.g. a list of indicators in the API):

```yaml
x-indicators: &indicators [GWP, ADPe]
api:
  ...
        select: *indicators
```

The `api` section, however, is read strictly: an unknown or misspelled key is an error. Errors are located:
`./lcaac.yaml:12:5: api.operations.vm.outputs.total.select: Unknown anchor 'indicatorz'.`

## Data sources

A data source is declared in a `.lca` file, with the location of its rows and the default value of each column:

```lca
datasource vm_inventory {
    location = "vm_inventory.csv"
    schema {
        id = "vm-001"
        vcpu = 2 u
        hours = 720 hour
    }
}
```

With the `csv` connector, `location` is relative to its `directory`. The CSV file has a header with the column names,
and must have every column of the schema: a missing one is an error when it is read
(`invalid index: 'vcpu' not in [id, hours]`). Quantity columns hold numbers, in the unit of the default value of the
column (`vcpu` in `u`, `hours` in `hour`). The default values give the type and unit of each column, and the row of
`default_record`. How a model reads data sources (`lookup`, `sum`, `for_each`) is in the skill `lcaac-modeling`.
