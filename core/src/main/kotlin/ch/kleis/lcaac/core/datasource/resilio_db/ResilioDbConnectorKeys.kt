package ch.kleis.lcaac.core.datasource.resilio_db

object ResilioDbConnectorKeys {
    const val RDB_CONNECTOR_NAME = "resilio_db"
    const val RDB_URL = "url"
    const val RDB_ACCESS_TOKEN = "accessToken"

    fun requiredOptionKeys() = setOf(
        RDB_URL, RDB_ACCESS_TOKEN,
    )
}

/*
    Example lcaac.yaml
    --
    connectors:
        - name: csv
          cache:
              enabled: true
              maxSize: 2048
              maxRecordsPerCacheLine: 8192
          options:
              directory: .
        - name: resilio_db
          cache:
              enabled: true
              maxSize: 2048
              maxRecordsPerCacheLine: 8192
          options:
              url: https://db.resilio.tech
              accessToken: <secret> # should be read from env var
    datasources:
        - name: hw_inventory
          connector: csv
        - name: hw_impacts
          connector: resilio_db
          options:
              # request
              primaryKey: id
              paramsFrom: hw_inventory
              foreignKey: id # hw_impacts will be joined with hw_inventory on pkey = fkey
              endpoint: rack_server

              # response
              lcStepKey: lc_step
              manufacturing: manufacturing
              transport: transport
              use: use
              endOfLife: end_of_life
    ```
 */
