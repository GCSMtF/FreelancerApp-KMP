package com.xommore.freelancerapp.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.xommore.freelancerapp.db.FreelancerDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = FreelancerDatabase.Schema,
            context = context,
            name = "freelancer.db"
        )
    }
}