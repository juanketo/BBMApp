package org.example.appbbmges.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.example.appbbmges.AppDatabaseBaby
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = System.getProperty("user.home") + File.separator + "AppDatabaseBaby.db"
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:$databasePath")

        if (!File(databasePath).exists()) {
            AppDatabaseBaby.Schema.create(driver)
        }

        return driver
    }
}