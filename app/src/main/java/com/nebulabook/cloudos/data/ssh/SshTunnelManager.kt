package com.nebulabook.cloudos.data.ssh

import android.util.Base64
import java.io.ByteArrayInputStream
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.nebulabook.cloudos.domain.model.TelemetryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SshTunnelManager {
    @Volatile private var session: Session? = null
    suspend fun connect(host: String, port: Int, username: String, privateKeyBase64: String, knownHosts: ByteArray) = withContext(Dispatchers.IO) {
        require(knownHosts.isNotEmpty()) { "A verified known_hosts entry is required" }
        val jsch = JSch()
        jsch.setKnownHosts(ByteArrayInputStream(knownHosts))
        jsch.addIdentity("nebula", Base64.decode(privateKeyBase64, Base64.DEFAULT), null, null)
        val s = jsch.getSession(username, host, port)
        s.setConfig("StrictHostKeyChecking", "yes")
        s.setServerAliveInterval(15_000)
        s.connect(20_000)
        s.setPortForwardingL("127.0.0.1", 3000, "127.0.0.1", 3000)
        session = s
    }
    fun execStreamingCommand(command: String): Flow<String> = flow {
        val s = session ?: error("SSH session is not connected")
        val channel = s.openChannel("exec") as ChannelExec
        channel.setCommand(command)
        val input = channel.inputStream
        channel.connect()
        val reader = input.bufferedReader()
        while (true) { val line = reader.readLine() ?: break; emit(line) }
        channel.disconnect()
    }
    suspend fun sampleTelemetry(): TelemetryState = withContext(Dispatchers.IO) {
        val s=session ?: return@withContext TelemetryState()
        val started=System.currentTimeMillis()
        val channel=s.openChannel("exec") as ChannelExec
        channel.setCommand("""free -m | awk 'NR==2{printf "%.1f %.1f", ${'$'}3/1024, ${'$'}2/1024}' && top -bn1 | grep "Cpu(s)" | awk '{print " " 100 - ${'$'}8}'""")
        val input=channel.inputStream; channel.connect(5_000)
        val text=input.bufferedReader().readText(); channel.disconnect()
        val parts=text.trim().split(Regex("\\s+")); val cpu=parts.getOrNull(2)?.toDoubleOrNull()?.toInt() ?: 0
        TelemetryState(cpu, parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0, parts.getOrNull(1)?.toDoubleOrNull() ?: 16.0, (System.currentTimeMillis()-started).toInt(), s.isConnected)
    }
    fun telemetryLoop(): Flow<TelemetryState> = flow { while (session?.isConnected == true) { emit(sampleTelemetry()); delay(5_000) } }
    fun disconnect() { try { session?.delPortForwardingL(3000) } catch (_: Exception) {}; session?.disconnect(); session=null }
}
