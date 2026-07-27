package com.mobile.tamatami.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mobile.tamatami.db.TamatamiDb

/**
 * JVM driver — used for host-side tests in this environment (no Android/iOS
 * runtime needed). Defaults to an in-memory database; the schema is created
 * eagerly so a fresh DB is immediately usable.
 */
actual class DriverFactory(private val url: String = JdbcSqliteDriver.IN_MEMORY) {
    actual fun createDriver(): SqlDriver =
        JdbcSqliteDriver(url).also { TamatamiDb.Schema.create(it) }
}
