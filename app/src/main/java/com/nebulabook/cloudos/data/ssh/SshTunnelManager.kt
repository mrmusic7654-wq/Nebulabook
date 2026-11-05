package com.nebulabook.cloudos.data.ssh

import android.util.Base64
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.nebulabook.cloudos.domain.model.TelemetryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SshTunnelManager {
    @Volatile private var session: Session? = null

    suspend fun connect(host:String,port:Int,username:String,privateKeyBase64:String,knownHosts:ByteArray) = withContext(Dispatchers.IO) {
        require(knownHosts.isNotEmpty()) { "A verified known_hosts entry is required" }
        val jsch=JSch()
        jsch.setKnownHosts(ByteArrayInputStream(knownHosts))
        jsch.addIdentity("nebula",Base64.decode(privateKeyBase64,Base64.DEFAULT),null,null)
        val candidate=jsch.getSession(username,host,port)
        candidate.setConfig("StrictHostKeyChecking","yes")
        candidate.setServerAliveInterval(15_000)
        candidate.connect(20_000)
        try { candidate.setPortForwardingL("127.0.0.1",3000,"127.0.0.1",3000) }
        catch(e:Exception) { candidate.disconnect(); throw e }
        session=candidate
    }

    fun execStreamingCommand(command:String):Flow<String> = flow {
        val s=session ?: error("SSH session is not connected")
        check(s.isConnected) { "SSH session has disconnected" }
        val channel=s.openChannel("exec") as ChannelExec
        val errors=ByteArrayOutputStream()
        channel.setCommand(command)
        channel.setErrStream(errors)
        val input=channel.inputStream
        try {
            channel.connect()
            val reader=input.bufferedReader()
            while(true) { val line=reader.readLine() ?: break; emit(line) }
            while(!channel.isClosed) delay(25)
            val errorText=errors.toString(Charsets.UTF_8.name()).trim()
            if(channel.exitStatus!=0) error("Remote command exited ${channel.exitStatus}${if(errorText.isNotBlank()) ": $errorText" else ""}")
            errorText.lineSequence().filter(String::isNotBlank).forEach { emit(it) }
        } finally { channel.disconnect() }
    }

    suspend fun sampleTelemetry():TelemetryState = withContext(Dispatchers.IO) {
        val s=session ?: return@withContext TelemetryState()
        if(!s.isConnected) return@withContext TelemetryState()
        val started=System.currentTimeMillis()
        val channel=s.openChannel("exec") as ChannelExec
        channel.setCommand("""free -m | awk 'NR==2{printf "%.1f %.1f", ${'$'}3/1024, ${'$'}2/1024}' && top -bn1 | grep "Cpu(s)" | awk '{print " " 100 - ${'$'}8}'""")
        val input=channel.inputStream
        channel.connect(5_000)
        val text=try { input.bufferedReader().readText() } finally { channel.disconnect() }
        val parts=text.trim().split(Regex("\\s+"))
        val cpu=parts.getOrNull(2)?.toDoubleOrNull()?.toInt() ?: return@withContext TelemetryState(isConnected=false)
        TelemetryState(cpu,parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0,parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0,(System.currentTimeMillis()-started).toInt(),s.isConnected)
    }

    fun telemetryLoop():Flow<TelemetryState> = flow {
        while(session?.isConnected==true) { emit(sampleTelemetry()); delay(5_000) }
    }
    fun disconnect() { try { session?.delPortForwardingL(3000) } catch(_:Exception) {}; session?.disconnect(); session=null }
}
