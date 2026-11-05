package com.nebulabook.cloudos

import android.app.Application
import com.nebulabook.cloudos.data.local.SecureVaultManager
import com.nebulabook.cloudos.data.ssh.SshTunnelManager
import kotlinx.coroutines.flow.MutableSharedFlow

class NebulaApplication : Application() {
    lateinit var vault: SecureVaultManager
        private set
    val sessionSsh: SshTunnelManager by lazy { SshTunnelManager() }
    val sessionStopped = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override fun onCreate() { super.onCreate(); vault = SecureVaultManager(this) }
}
