package core.database

import io.r2dbc.spi.Connection
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.ParamType
import org.jooq.conf.Settings
import org.jooq.conf.StatementType
import org.jooq.impl.DSL

data class DatabaseConnection(
    val connection: Connection,
    val localCaches: LocalCaches,
) {

    val jooq: DSLContext = DSL.using(
        this.connection,
        SQLDialect.POSTGRES,
        Settings()
            .withStatementType(StatementType.PREPARED_STATEMENT)
            .withParamType(ParamType.INDEXED)
    )

}
