package ch.kleis.lcaac.core.datasource.resilio_db

object ResilioDbConnectorKeys {
    const val RDB_CONNECTOR_NAME = "resilio_db"
    const val RDB_PRIMARY_KEY = "primaryKey"
    const val RDB_PARAMS_FROM = "paramsFrom"
    const val RDB_FOREIGN_KEY = "foreignKey"
    const val RDB_URL = "url"
    const val RDB_ENDPOINT = "endpoint"
    const val RDB_ACCESS_TOKEN = "accessToken"
    const val RDB_LC_STEP_KEY = "lc_step"
    const val RDB_MANUFACTURING = "manufacturing"
    const val RDB_TRANSPORT = "transport"
    const val RDB_USE = "use"
    const val RDB_END_OF_LIFE = "endOfLife"

    fun requiredOptionKeys() = setOf(
        RDB_PRIMARY_KEY,
        RDB_PARAMS_FROM, RDB_FOREIGN_KEY,
        RDB_URL, RDB_ENDPOINT, RDB_ACCESS_TOKEN,
        RDB_LC_STEP_KEY,
        RDB_MANUFACTURING, RDB_TRANSPORT,
        RDB_USE, RDB_END_OF_LIFE
    )
}

/*
    Example lcaac.yaml
    --
    ...
    datasources:
        - hw_inventory:
            connector: csv
            ...
        - hw_impacts:
            connector: resilio_db
            cache:
                enabled: true
                maxSize: 2048
                maxRecordsPerCacheLine: 8192
            options:
                primaryKey: id
                paramsFrom: hw_inventory
                foreignKey: id # hw_impacts will be joined with hw_inventory on pkey = fkey
                url: https://db.resilio.tech
                endpoint: server_rack
                accessToken: <secret> # should be read from env var
                manufacturing: manufacturing
                transport: transport
                use: use
                endOfLife: end_of_life
    ```
 */
