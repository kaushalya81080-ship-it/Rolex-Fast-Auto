package com.example

import android.app.Application
import com.example.data.local.RolexDatabase
import com.example.data.repository.RolexRepository

class RolexApplication : Application() {
    lateinit var database: RolexDatabase
        private set

    lateinit var repository: RolexRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = RolexDatabase.getDatabase(this)
        repository = RolexRepository(database.rolexDao())
    }
}
