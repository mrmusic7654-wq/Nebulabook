package com.nebulabook.cloudos.data.local

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import java.io.ByteArrayOutputStream

class SecureVaultManager(context: Context) {
    private val prefs = try {
        val master = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(context, "nebula_vault", master,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    } catch (_: Exception) { context.getSharedPreferences("nebula_vault_fallback", Context.MODE_PRIVATE) }

    fun get(key: String): String = prefs.getString(key, "") ?: ""
    fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    fun isSimulation(): Boolean = prefs.getBoolean("simulation", true)
    fun setSimulation(value: Boolean) { prefs.edit().putBoolean("simulation", value).apply() }
    fun publicKey(): String = get("ssh_public_key")
    fun generateSshKeyPair(): String {
        val pair = KeyPair.genKeyPair(JSch(), KeyPair.RSA, 4096)
        val privateOut = ByteArrayOutputStream(); pair.writePrivateKey(privateOut)
        val public = "ssh-rsa ${Base64.encodeToString(pair.publicKeyBlob, Base64.NO_WRAP)} nebulabook@android"
        put("ssh_private_key", Base64.encodeToString(privateOut.toByteArray(), Base64.NO_WRAP))
        put("ssh_public_key", public); pair.dispose(); return public
    }
}
