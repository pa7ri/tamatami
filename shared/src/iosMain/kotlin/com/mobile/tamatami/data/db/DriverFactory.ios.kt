package com.mobile.tamatami.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.mobile.tamatami.db.TamatamiDb

actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(TamatamiDb.Schema, "tamatami.db")
}
