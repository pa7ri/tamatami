package com.mobile.tamatami.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-provided SQL driver. Android supplies an AndroidSqliteDriver (needs a
 * Context), iOS a NativeSqliteDriver, JVM/tests a JdbcSqliteDriver. The [context]
 * parameter is only meaningful on Android; other platforms ignore it.
 */
expect class DriverFactory {
    fun createDriver(): SqlDriver
}
