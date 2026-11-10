package com.petshop.pos

import android.app.Application
import com.petshop.pos.data.local.PetShopDatabase
import com.petshop.pos.data.repository.PetShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PetShopApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { PetShopDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { PetShopRepository(database) }

    companion object {
        lateinit var instance: PetShopApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
