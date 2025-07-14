package org.example.appbbmges.data

import app.cash.sqldelight.db.SqlDriver
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.example.appbbmges.AppDatabaseBaby

actual class DatabaseDriverFactory (private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = AppDatabaseBaby.Schema,
            context = context,
            name = "AppDatabaseBaby.db"
        )
    }
}