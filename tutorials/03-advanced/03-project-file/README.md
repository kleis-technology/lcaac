# Project description file

A LCA as Code project can be specified with a YAML configuration.
In this tutorial, the configuration file is used to specify:
- the project name
- the project description
- and customizing the CSV connector

Hence, the file `lcaac.yaml` contains
```yaml
name: Project file
description: Sample project to illustrate the use of lcaac.yaml
connectors:
  - name: csv
    options:
      directory: data
```

while the file `lcaac-mock.yaml` contains
```yaml
name: Project file
description: Sample project to illustrate the use of lcaac.yaml
connectors:
  - name: csv
    options:
      directory: mock
```

Here each file specifies a different location for the folder containing the CSV files supporting the datasources.
You can choose which settings to use with the cli option `-p` or `--project`.
```bash
lcaac assess --project lcaac.yaml main
lcaac assess --project lcaac-mock.yaml main
```
