package com.nebulabook.cloudos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nebulabook.cloudos.NebulaApplication
import com.nebulabook.cloudos.data.local.QuotaDataStore
import com.nebulabook.cloudos.data.ssh.ProvisioningScripts
import com.nebulabook.cloudos.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

 data class NebulaUiState(
    val simulation: Boolean=true, val engine: CloudEngine=CloudEngine.GOOGLE, val booting:Boolean=false, val connected:Boolean=false,
    val logs:List<String> = emptyList(), val telemetry:TelemetryState=TelemetryState(), val selectedTab:Int=0,
    val installed:Set<String> = emptySet(), val selectedFilter:String="All", val renderJob:KaggleRenderJob?=null,
    val notice:String="", val trackpad:Boolean=false, val ribbon:Boolean=true, val workspaceWindow:String="Blender 4.2"
)

class NebulaMainViewModel(app: Application) : AndroidViewModel(app) {
    private val vault=(app as NebulaApplication).vault
    private val quota=QuotaDataStore(app)
    private val _state=MutableStateFlow(NebulaUiState(simulation=vault.isSimulation()))
    val state=_state.asStateFlow()
    fun setTab(tab:Int)=_state.update { it.copy(selectedTab=tab) }
    fun setEngine(engine:CloudEngine)=_state.update { it.copy(engine=engine) }
    fun toggleSimulation() { val v=!_state.value.simulation; vault.setSimulation(v); _state.update { it.copy(simulation=v, notice=if(v) "Interactive simulation enabled" else "Live mode selected — configure credentials and a supported endpoint in Vault") } }
    fun notice(message:String) { _state.update { it.copy(notice=message) } }
    fun setFilter(value:String)=_state.update { it.copy(selectedFilter=value) }
    fun toggleTrackpad()=_state.update { it.copy(trackpad=!it.trackpad) }
    fun toggleRibbon()=_state.update { it.copy(ribbon=!it.ribbon) }
    fun chooseWindow(window:String)=_state.update { it.copy(workspaceWindow=window) }
    fun boot() {
        if(!_state.value.simulation) { notice("Live startup needs a provider-supported SSH endpoint. Cloud Shell REST does not return generic SSH host/port credentials; no connection was claimed."); return }
        if(_state.value.booting || _state.value.connected) return
        viewModelScope.launch {
            _state.update { it.copy(booting=true, logs=emptyList(), notice="") }
            val sequence=listOf("Simulation mode: no cloud resources will be created.","Authenticating selected engine (local demo credentials).","Waking Linux VM and establishing SSH tunnel 127.0.0.1:3000…","Mounting shared drive (demo state only)…","Starting Ubuntu XFCE desktop simulation…","NEBULABOOK_READY")
            sequence.forEach { line -> delay(650); _state.update { it.copy(logs=it.logs+line) } }
            _state.update { it.copy(booting=false, connected=true, telemetry=TelemetryState(28,5.4,16.0,22,true), notice="Demo desktop is ready") }
            refreshDemoTelemetry()
        }
    }
    fun closeLid() { _state.update { it.copy(connected=false,booting=false,telemetry=TelemetryState(),notice="Session closed. Simulation consumed no cloud quota.") } }
    fun install(name:String) {
        if(!_state.value.simulation) { notice("No live SSH host is configured. Installation was not sent."); return }
        _state.update { it.copy(installed=it.installed+name, notice="$name marked installed in this demo") }
    }
    fun dispatchRender(file:String,start:Int,end:Int) {
        if(!_state.value.simulation) { notice("Kaggle credentials and a valid uploaded kernel/dataset are required. No GPU job was dispatched."); return }
        if(end<start) { notice("End frame must be after the start frame."); return }
        viewModelScope.launch {
            _state.update { it.copy(renderJob=KaggleRenderJob(file,start,end,"Queued · simulation",0),notice="Render simulated locally; no Kaggle GPU was used") }
            for(p in listOf(8,21,44,67,91,100)) { delay(650); _state.update { it.copy(renderJob=it.renderJob?.copy(progress=p,status=if(p==100) "Complete · simulated" else "Rendering · simulation")) } }
        }
    }
    fun saveVault(key:String,value:String) { vault.put(key,value); notice("Saved securely on this device") }
    fun readVault(key:String)=vault.get(key)
    fun generateKey() { viewModelScope.launch(Dispatchers.IO) { try { val public=vault.generateSshKeyPair(); notice("RSA-4096 key generated · ${public.take(28)}…") } catch(e:Exception) { notice("Key generation failed: ${e.message}") } } }
    fun publicKey()=vault.publicKey()
    fun installCommand(name:String)=ProvisioningScripts.installCommands[name].orEmpty()
    fun refreshDemoTelemetry() {
        if(_state.value.connected && _state.value.simulation) viewModelScope.launch { while(true) { delay(2500); if(!_state.value.connected) break; _state.update { it.copy(telemetry=TelemetryState((18..67).random(),(3..8).random().toDouble(),16.0,(18..65).random(),true)) } } }
    }
}
