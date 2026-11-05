package com.nebulabook.cloudos.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.nebulabook.cloudos.NebulaApplication
import com.nebulabook.cloudos.data.remote.GitHubCodespacesApi
import com.nebulabook.cloudos.data.remote.GoogleCloudShellApi
import com.nebulabook.cloudos.data.remote.KaggleRenderApi
import com.nebulabook.cloudos.data.ssh.ProvisioningScripts
import com.nebulabook.cloudos.data.service.LaptopLidForegroundService
import com.nebulabook.cloudos.domain.model.TelemetryState
import com.nebulabook.cloudos.domain.model.CodespaceInfo
import com.nebulabook.cloudos.domain.model.CodespaceMachine
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
    val codespaces: List<CodespaceInfo> = emptyList(),
    val selectedCodespace: String = "",
    val codespaceMachines: List<CodespaceMachine> = emptyList(),
    val codespaceBusy: Boolean = false,
    val codespaceStatus: String = "",
    val cloudShellBusy: Boolean = false,
    val cloudShellStatus: String = "",
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
    private val googleCloudShell = GoogleCloudShellApi()
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
            _state.update { it.copy(githubBusy=true,notice="Verifying GitHub token and retrieving Codespaces…") }
            try {
                val (login,spaces) = withContext(Dispatchers.IO) {
                    val user=org.json.JSONObject(github.user(token)).optString("login")
                    val response=org.json.JSONObject(github.list(token))
                    user to parseCodespaces(response.optJSONArray("codespaces") ?: JSONArray())
                }
                val first=spaces.firstOrNull()?.name.orEmpty()
                val machines=if(first.isNotBlank()) withContext(Dispatchers.IO) { parseMachines(github.machines(token,first)) } else emptyList()
                _state.update { it.copy(githubBusy=false,githubAccount=login,codespaces=spaces,selectedCodespace=first,codespaceMachines=machines,codespaceStatus="",notice="GitHub authenticated as @$login. Codespace state and machine data are from GitHub.") }
            } catch(e:Exception) {
                _state.update { it.copy(githubBusy=false,githubAccount="",codespaces=emptyList(),codespaceMachines=emptyList(),notice="GitHub verification failed: ${e.message ?: "network error"}") }
            }
        }
    }

    private fun parseCodespaces(items:JSONArray):List<CodespaceInfo> = (0 until items.length()).mapNotNull { index ->
        val item=items.optJSONObject(index) ?: return@mapNotNull null
        val machine=item.optJSONObject("machine")
        val repository=item.optJSONObject("repository")
        val name=item.optString("name").takeIf(String::isNotBlank) ?: return@mapNotNull null
        CodespaceInfo(name,item.optString("state","Unknown"),repository?.optString("full_name").orEmpty(),item.optString("web_url"),machine?.optString("name").orEmpty(),machine?.optInt("cpus",0) ?: 0,machine?.optLong("memory_in_bytes",0L) ?: 0L,machine?.optLong("storage_in_bytes",0L) ?: 0L)
    }

    private fun parseMachines(response:String):List<CodespaceMachine> {
        val items=org.json.JSONObject(response).optJSONArray("machines") ?: JSONArray()
        return (0 until items.length()).mapNotNull { i ->
            val item=items.optJSONObject(i) ?: return@mapNotNull null
            val name=item.optString("name").takeIf(String::isNotBlank) ?: return@mapNotNull null
            CodespaceMachine(name,item.optString("display_name",name),item.optInt("cpus",0),item.optLong("memory_in_bytes",0L),item.optLong("storage_in_bytes",0L))
        }
    }

    fun selectCodespace(name:String) {
        val token=vault.get("github_pat_token").trim()
        _state.update { it.copy(selectedCodespace=name,codespaceMachines=emptyList(),codespaceStatus="Loading available machine types…") }
        if(token.isBlank()) return
        viewModelScope.launch {
            try {
                val machines=withContext(Dispatchers.IO) { parseMachines(github.machines(token,name)) }
                _state.update { it.copy(codespaceMachines=machines,codespaceStatus="Machine options refreshed from GitHub.") }
            } catch(e:Exception) { _state.update { it.copy(codespaceStatus="Could not load machine options: ${e.message}") } }
        }
    }

    fun startCodespace(name:String) {
        val token=vault.get("github_pat_token").trim()
        if(token.isBlank()) { notice("Verify your GitHub token first."); return }
        val selected=_state.value.codespaces.firstOrNull { it.name==name }
        if(selected==null) { notice("Refresh GitHub Codespaces and select a workspace."); return }
        val twoCore=_state.value.codespaceMachines.filter { it.cpus==2 && it.memoryBytes>0 && it.storageBytes>0 }.minByOrNull { it.memoryBytes }
        if(twoCore==null) { notice("GitHub reports no available 2-core machine for this Codespace. No larger machine was selected."); return }
        if(selected.state.equals("Available",true) && selected.machineName!=twoCore.name) { notice("This Codespace is running on another machine type. Stop it first; then start it to request the available 2-core machine."); return }
        viewModelScope.launch {
            _state.update { it.copy(codespaceBusy=true,codespaceStatus="Requesting 2-core machine from GitHub…") }
            try {
                if(selected.state.equals("Available",true)) {
                    _state.update { it.copy(codespaceStatus="GitHub reports this Codespace is already available on the 2-core machine.") }
                } else {
                    if(!selected.state.contains("shutdown",true) && !selected.state.equals("stopped",true)) error("Codespace is in state ${selected.state}; wait for GitHub to finish that operation before starting it.")
                    if(selected.machineName!=twoCore.name) withContext(Dispatchers.IO) { github.setMachine(token,name,twoCore.name) }
                    withContext(Dispatchers.IO) { github.start(token,name) }
                    _state.update { it.copy(codespaceStatus="GitHub accepted the start request. Waiting for its reported state…") }
                    var latest=selected
                    for(attempt in 0 until 60) {
                        kotlinx.coroutines.delay(5_000)
                        latest=withContext(Dispatchers.IO) { parseCodespaces(org.json.JSONArray().apply { put(org.json.JSONObject(github.get(token,name))) }).firstOrNull() ?: latest }
                        _state.update { state -> state.copy(codespaces=state.codespaces.map { if(it.name==name) latest else it },codespaceStatus="GitHub state: ${latest.state}") }
                        if(latest.state.equals("Available",true) || latest.state.contains("failed",true)) break
                    }
                }
                refreshCodespaces(token,selected.name)
            } catch(e:Exception) { _state.update { it.copy(codespaceStatus="GitHub start failed: ${e.message ?: "request error"}") } }
            finally { _state.update { it.copy(codespaceBusy=false) } }
        }
    }

    fun stopCodespace(name:String) {
        val token=vault.get("github_pat_token").trim()
        if(token.isBlank()) { notice("Verify your GitHub token first."); return }
        viewModelScope.launch {
            _state.update { it.copy(codespaceBusy=true,codespaceStatus="Requesting Codespace stop from GitHub…") }
            try {
                withContext(Dispatchers.IO) { github.stop(token,name) }
                var latest=_state.value.codespaces.firstOrNull { it.name==name }
                for(attempt in 0 until 60) {
                    kotlinx.coroutines.delay(5_000)
                    latest=withContext(Dispatchers.IO) { parseCodespaces(org.json.JSONArray().apply { put(org.json.JSONObject(github.get(token,name))) }).firstOrNull() ?: latest }
                    _state.update { state -> state.copy(codespaces=state.codespaces.map { if(it.name==name) latest ?: it else it },codespaceStatus="GitHub state: ${latest?.state ?: "unknown"}") }
                    if(latest?.state?.contains("shutdown",true)==true || latest?.state.equals("stopped",true)) break
                }
                refreshCodespaces(token,name)
            } catch(e:Exception) { _state.update { it.copy(codespaceStatus="GitHub stop failed: ${e.message ?: "request error"}") } }
            finally { _state.update { it.copy(codespaceBusy=false) } }
        }
    }

    private suspend fun refreshCodespaces(token:String,selectedName:String) {
        val (spaces,machines)=withContext(Dispatchers.IO) {
            val list=parseCodespaces(org.json.JSONObject(github.list(token)).optJSONArray("codespaces") ?: JSONArray())
            val machineList=if(selectedName.isNotBlank()) parseMachines(github.machines(token,selectedName)) else emptyList()
            list to machineList
        }
        _state.update { it.copy(codespaces=spaces,codespaceMachines=machines,codespaceStatus=spaces.firstOrNull { it.name==selectedName }?.state ?: "No Codespace state returned") }
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
        val host=vault.get("ssh_host").trim()
        val port=vault.get("ssh_port").toIntOrNull() ?: 22
        val username=vault.get("ssh_username").trim()
        val privateKey=vault.get("ssh_private_key")
        val knownHosts=vault.get("ssh_known_hosts").trim()
        val missing=listOf("SSH host" to host,"SSH username" to username,"SSH private key" to privateKey,"verified known_hosts entry" to knownHosts).firstOrNull { it.second.isBlank() }
        if(missing!=null) { notice("Configure ${missing.first} in Settings before connecting."); return }
        if(_state.value.booting || _state.value.connected) return
        viewModelScope.launch { _state.update { it.copy(booting=true,logs=emptyList(),notice="") }; connectRemote(host,port,username,privateKey,knownHosts) }
    }

    fun connectGoogleCloudShell() {
        val token=vault.get("google_oauth_token").trim()
        val publicKey=vault.publicKey().trim()
        val privateKey=vault.get("ssh_private_key")
        if(token.isBlank()) { notice("Save a Google OAuth access token with Cloud Shell API scope in Settings."); return }
        if(publicKey.isBlank() || privateKey.isBlank()) { notice("Generate an SSH keypair in Settings first."); return }
        if(_state.value.booting || _state.value.connected) return
        viewModelScope.launch {
            _state.update { it.copy(booting=true,cloudShellBusy=true,logs=listOf("Requesting the signed-in user's Cloud Shell environment from Google."),telemetry=TelemetryState(),cloudShellStatus="Calling Cloud Shell API…",notice="") }
            try {
                val endpoint=withContext(Dispatchers.IO) {
                    val cleanKey=publicKey.split(Regex("\\s+")).take(2).joinToString(" ")
                    var current=runCatching { org.json.JSONObject(googleCloudShell.getEnvironment(token)) }.getOrNull()
                    val isRunning=current?.optString("state").equals("RUNNING",true)
                    val keys=current?.optJSONArray("publicKeys")
                    val keyPresent=keys!=null && (0 until keys.length()).any { keys.optString(it)==cleanKey }
                    if(!isRunning) googleCloudShell.start(token,if(keyPresent) null else cleanKey)
                    else if(!keyPresent) googleCloudShell.addPublicKey(token,cleanKey)
                    var last=current ?: org.json.JSONObject()
                    var ready=false
                    for(attempt in 0 until 60) {
                        if(attempt>0) kotlinx.coroutines.delay(3_000)
                        last=org.json.JSONObject(googleCloudShell.getEnvironment(token))
                        val state=last.optString("state","UNKNOWN")
                        _state.update { current -> current.copy(cloudShellStatus="Cloud Shell API state: $state",logs=current.logs+"Cloud Shell API state: $state") }
                        val remoteKeys=last.optJSONArray("publicKeys")
                        val hasKey=remoteKeys!=null && (0 until remoteKeys.length()).any { remoteKeys.optString(it)==cleanKey }
                        if(state.equals("RUNNING",true) && hasKey && last.optString("sshHost").isNotBlank() && last.optInt("sshPort")>0 && last.optString("sshUsername").isNotBlank()) { ready=true; break }
                    }
                    if(!ready) error("Cloud Shell did not report a running SSH endpoint and the registered public key before timeout.")
                    Triple(last.getString("sshHost"),last.getInt("sshPort"),last.getString("sshUsername"))
                }
                val (host,port,username)=endpoint
                vault.put("ssh_host",host); vault.put("ssh_port",port.toString()); vault.put("ssh_username",username)
                _state.update { it.copy(sshHost=host,cloudShellStatus="Cloud Shell running · $host:$port") }
                val knownHosts=vault.get("ssh_known_hosts").trim()
                if(knownHosts.isBlank()) {
                    _state.update { it.copy(booting=false,cloudShellStatus="Cloud Shell is running; SSH host verification is still required.",notice="Cloud Shell endpoint: $host:$port. From a trusted computer run: ssh-keyscan -p $port $host. Verify the fingerprint independently, save the resulting known_hosts line in Settings, then tap Connect Google Cloud Shell again.") }
                } else connectRemote(host,port,username,privateKey,knownHosts)
            } catch(e:Exception) {
                ssh.disconnect()
                _state.update { it.copy(booting=false,connected=false,cloudShellStatus="Cloud Shell request failed",notice="Cloud Shell connection failed: ${e.message ?: "API error"}") }
            } finally { _state.update { it.copy(cloudShellBusy=false) } }
        }
    }

    private suspend fun connectRemote(host:String,port:Int,username:String,privateKey:String,knownHosts:String) {
        _state.update { it.copy(booting=true,telemetry=TelemetryState(),sshHost=host,notice="Connecting to $host:$port with strict host-key verification…") }
        try {
            withContext(Dispatchers.IO) { ssh.connect(host,port,username,privateKey,knownHosts.toByteArray()) }
            _state.update { it.copy(logs=it.logs+"SSH authenticated to $host:$port (host key verified).") }
            withContext(Dispatchers.IO) {
                ssh.execStreamingCommand(ProvisioningScripts.BOOT_WEBTOP_SCRIPT).collect { line ->
                    withContext(Dispatchers.Main) { _state.update { state -> state.copy(logs=state.logs+line) } }
                }
            }
            if(_state.value.logs.none { it.contains("NEBULABOOK_READY") }) error("Remote Webtop endpoint did not become ready. Check the actual SSH output and host Docker support.")
            _state.update { it.copy(booting=false,connected=true,sessionStartedAt=System.currentTimeMillis(),notice="Remote desktop is ready on the verified SSH tunnel.") }
            ContextCompat.startForegroundService(nebulaApp,Intent(nebulaApp,LaptopLidForegroundService::class.java))
            viewModelScope.launch { ssh.telemetryLoop().collect { sample -> _state.update { it.copy(telemetry=sample) } } }
        } catch(e:Exception) {
            withContext(Dispatchers.IO) {
                runCatching { ssh.execStreamingCommand("docker stop nebulabook_os 2>/dev/null || true").collect { } }
                ssh.disconnect()
            }
            nebulaApp.stopService(Intent(nebulaApp,LaptopLidForegroundService::class.java))
            _state.update { it.copy(booting=false,connected=false,sessionStartedAt=0L,telemetry=TelemetryState(),notice="SSH startup failed: ${e.message ?: "connection error"}") }
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
