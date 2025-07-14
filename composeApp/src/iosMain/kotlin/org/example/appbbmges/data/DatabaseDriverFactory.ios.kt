package org.example.appbbmges.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.example.appbbmges.AppDatabaseBaby

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = AppDatabaseBaby.Schema,
            name = "AppDatabaseBaby.db"
        )
    }
}