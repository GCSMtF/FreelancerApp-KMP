package com.xommore.freelancerapp.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.xommore.freelancerapp.db.FreelancerDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = FreelancerDatabase.Schema,
            name = "freelancer.db"
        )
    }
}