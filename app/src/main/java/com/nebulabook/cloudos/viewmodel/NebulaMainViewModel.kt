package com.nebulabook.cloudos.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.nebulabook.cloudos.NebulaApplication
import com.nebulabook.cloudos.data.remote.GitHubCodespacesApi
import com.nebulabook.cloudos.data.remote.KaggleRenderApi
import com.nebulabook.cloudos.data.ssh.ProvisioningScripts
import com.nebulabook.cloudos.data.service.LaptopLidForegroundService
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.domain.model.desktopApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

 data class NebulaUiState(
    val booting: Boolean = false,
    val connected: Boolean = false,
    val sshHost: String = "",
    val sessionStartedAt: Long = 0L,
    val logs: List<String> = emptyList(),
    val telemetry: TelemetryState = TelemetryState(),
    val selectedFilter: String = "All",
    val installed: Set<String> = emptySet(),
    val activeInstall: String = "",
    val installLogs: List<String> = emptyList(),
    val githubAccount: String = "",
    val codespaces: List<String> = emptyList(),
    val githubBusy: Boolean = false,
    val kaggleStatus: String = "",
    val notice: String = "",
    val trackpad: Boolean = false,
    val ribbon: Boolean = true
)

class NebulaMainViewModel(app: Application) : AndroidViewModel(app) {
    private val nebulaApp = app as NebulaApplication
    private val vault = nebulaApp.vault
    private val ssh = nebulaApp.sessionSsh
    private val github = GitHubCodespacesApi()
    private val kaggle = KaggleRenderApi()
    private val _state = MutableStateFlow(NebulaUiState(sshHost = vault.get("ssh_host")))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            nebulaApp.sessionStopped.collect {
                _state.update { it.copy(connected=false,booting=false,sessionStartedAt=0L,telemetry=TelemetryState(),notice="Foreground action disconnected SSH and requested container shutdown; verify the host if it was unreachable.") }
            }
        }
    }

    fun notice(message: String) { _state.update { it.copy(notice = message) } }
    fun setFilter(value: String) { _state.update { it.copy(selectedFilter = value) } }
    fun toggleTrackpad() { _state.update { it.copy(trackpad = !it.trackpad) } }
    fun toggleRibbon() { _state.update { it.copy(ribbon = !it.ribbon) } }
    fun saveVault(key: String, value: String) { vault.put(key, value); if (key == "ssh_host") _state.update { it.copy(sshHost = value) } }
    fun readVault(key: String) = vault.get(key)
    fun publicKey() = vault.publicKey()
    fun generateKey() { viewModelScope.launch(Dispatchers.IO) { try { vault.generateSshKeyPair(); notice("RSA-4096 key generated. Add the public key to your SSH server, then connect.") } catch (e: Exception) { notice("Key generation failed: ${e.message}") } } }

    fun verifyGitHub() {
        val token = vault.get("github_pat_token").trim()
        if (token.isBlank()) { notice("Enter and save a GitHub token first."); return }
        viewModelScope.launch {
            _state.update { it.copy(githubBusy = true, notice = "Verifying GitHub token…") }
            try {
                val (login, spaces) = withContext(Dispatchers.IO) {
                    val userJson = org.json.JSONObject(github.user(token))
                    val response = org.json.JSONObject(github.list(token))
                    val list = response.optJSONArray("codespaces") ?: JSONArray()
                    val names = (0 until list.length()).mapNotNull { i -> list.optJSONObject(i)?.optString("name")?.takeIf(String::isNotBlank) }
                    userJson.optString("login") to names
                }
                _state.update { it.copy(githubBusy = false, githubAccount = login, codespaces = spaces, notice = "GitHub authenticated as $login · ${spaces.size} Codespaces returned by API") }
            } catch (e: Exception) {
                _state.update { it.copy(githubBusy = false, githubAccount = "", codespaces = emptyList(), notice = "GitHub verification failed: ${e.message ?: "network error"}") }
            }
        }
    }

    fun checkKaggleStatus(slug: String) {
        val user = vault.get("kaggle_username").trim()
        val key = vault.get("kaggle_api_key").trim()
        if (user.isBlank() || key.isBlank()) { notice("Save Kaggle username and API key in Vault first."); return }
        if (slug.isBlank()) { notice("Enter an existing kernel slug."); return }
        viewModelScope.launch {
            _state.update { it.copy(kaggleStatus = "Fetching status…") }
            try {
                val response = withContext(Dispatchers.IO) { kaggle.status(user, key, slug) }
                _state.update { it.copy(kaggleStatus = response, notice = "Status received directly from Kaggle.") }
            } catch (e: Exception) {
                _state.update { it.copy(kaggleStatus = "", notice = "Kaggle status request failed: ${e.message ?: "network error"}") }
            }
        }
    }

    fun boot() {
        val host = vault.get("ssh_host").trim()
        val port = vault.get("ssh_port").toIntOrNull() ?: 22
        val username = vault.get("ssh_username").trim()
        val key = vault.get("ssh_private_key")
        val knownHosts = vault.get("ssh_known_hosts").trim()
        val missing = listOf("SSH host" to host, "SSH username" to username, "SSH private key" to key, "known_hosts entry" to knownHosts).firstOrNull { it.second.isBlank() }
        if (missing != null) { notice("Configure ${missing.first} and save it in Vault before connecting."); return }
        if (_state.value.booting || _state.value.connected) return
        viewModelScope.launch {
            _state.update { it.copy(booting = true, logs = emptyList(), telemetry = TelemetryState(), notice = "Connecting to $host:$port…") }
            try {
                withContext(Dispatchers.IO) { ssh.connect(host, port, username, key, knownHosts.toByteArray()) }
                _state.update { it.copy(logs = it.logs + "SSH authenticated to $host:$port (host key verified).", sshHost = host) }
                withContext(Dispatchers.IO) {
                    ssh.execStreamingCommand(ProvisioningScripts.BOOT_WEBTOP_SCRIPT).collect { line ->
                        withContext(Dispatchers.Main) { _state.update { state -> state.copy(logs = state.logs + line) } }
                    }
                }
                if (_state.value.logs.none { it.contains("NEBULABOOK_READY") }) error("Desktop provisioning did not report NEBULABOOK_READY. Check SSH output and Docker availability.")
                _state.update { it.copy(booting = false, connected = true, sessionStartedAt = System.currentTimeMillis(), notice = "Desktop is ready on the verified SSH tunnel.") }
                ContextCompat.startForegroundService(nebulaApp, Intent(nebulaApp, LaptopLidForegroundService::class.java))
                viewModelScope.launch { ssh.telemetryLoop().collect { sample -> _state.update { it.copy(telemetry = sample) } } }
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    runCatching { ssh.execStreamingCommand("docker stop nebulabook_os 2>/dev/null || true").collect { } }
                    ssh.disconnect()
                }
                nebulaApp.stopService(Intent(nebulaApp, LaptopLidForegroundService::class.java))
                _state.update { it.copy(booting = false, connected = false, sessionStartedAt = 0L, telemetry = TelemetryState(), notice = "SSH startup failed: ${e.message ?: "connection error"}") }
            }
        }
    }

    fun closeLid() {
        viewModelScope.launch {
            _state.update { it.copy(notice = "Stopping remote desktop…") }
            val containerStopConfirmed = withContext(Dispatchers.IO) {
                var stopped = false
                if (_state.value.connected) stopped = runCatching { ssh.execStreamingCommand("docker stop nebulabook_os").collect { } }.isSuccess
                ssh.disconnect()
                stopped
            }
            nebulaApp.stopService(Intent(nebulaApp, LaptopLidForegroundService::class.java))
            _state.update { it.copy(connected = false, booting = false, sessionStartedAt = 0L, telemetry = TelemetryState(), notice = if(containerStopConfirmed) "Remote container stopped; SSH tunnel closed." else "SSH tunnel closed, but remote container shutdown was not confirmed. Check the SSH host.") }
        }
    }

    fun install(name: String) {
        val command = ProvisioningScripts.installCommands[name]
        if (command == null) { notice("No install command is configured for $name."); return }
        if (!_state.value.connected) { notice("Connect to your SSH desktop before installing software."); return }
        val escaped = command.replace("'", "'\\''")
        val remote = "docker exec -u 0 nebulabook_os bash -lc '$escaped'"
        viewModelScope.launch {
            _state.update { it.copy(activeInstall = name, installLogs = listOf("$ $remote"), notice = "Installing $name on ${it.sshHost}…") }
            try {
                withContext(Dispatchers.IO) {
                    ssh.execStreamingCommand(remote).collect { line -> withContext(Dispatchers.Main) { _state.update { it.copy(installLogs = it.installLogs + line) } } }
                }
                _state.update { it.copy(installed = it.installed + name, activeInstall = "", notice = "$name install command finished on ${it.sshHost}.") }
            } catch (e: Exception) {
                _state.update { it.copy(activeInstall = "", notice = "$name installation failed: ${e.message}") }
            }
        }
    }

}
