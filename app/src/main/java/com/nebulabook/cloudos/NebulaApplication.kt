package com.nebulabook.cloudos

import android.app.Application
import com.nebulabook.cloudos.data.local.SecureVaultManager

class NebulaApplication : Application() {
    lateinit var vault: SecureVaultManager
        private set
    override fun onCreate() { super.onCreate(); vault = SecureVaultManager(this) }
}
