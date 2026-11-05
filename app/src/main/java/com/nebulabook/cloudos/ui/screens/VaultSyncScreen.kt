package com.nebulabook.cloudos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import com.nebulabook.cloudos.ui.components.GlassCard
import com.nebulabook.cloudos.ui.theme.*

@Composable fun VaultSyncScreen(read:(String)->String, save:(String,String)->Unit, publicKey:()->String, generate:()->Unit, onVerify:()->Unit, githubBusy:Boolean, githubAccount:String, codespaces:List<String>) {
    val clip=LocalClipboardManager.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("VAULT & SYNC",style=MaterialTheme.typography.headlineMedium,color=TextPrimary)
        GlassCard { Text("DEVICE SSH KEYPAIR",color=NeonCyan,style=MaterialTheme.typography.titleMedium); val pub=publicKey(); Text(if(pub.isBlank()) "No SSH public key generated" else pub, color=TextSecondary,style=MaterialTheme.typography.labelSmall); Row { Button(onClick=generate) { Text("GENERATE RSA-4096") }; TextButton(onClick={if(pub.isNotBlank()) clip.setText(AnnotatedString(pub))}) { Text("COPY PUBLIC KEY") } } }
        Text("SSH DESKTOP TARGET",color=NeonCyan,style=MaterialTheme.typography.titleMedium)
        CredentialCard("SSH HOST", "ssh_host", "Host name or IP", read,save,false)
        CredentialCard("SSH PORT", "ssh_port", "Port (usually 22)", read,save,false)
        CredentialCard("SSH USERNAME", "ssh_username", "Remote Linux username", read,save,false)
        CredentialCard("SSH KNOWN HOSTS", "ssh_known_hosts", "Verified known_hosts line for this host", read,save,false)
        Text("Generate a key below, add the public key to the remote account’s authorized_keys, then connect. The host fingerprint is required; the app will not disable host verification.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
        CredentialCard("GITHUB API TOKEN", "github_pat_token", "Fine-grained token with Codespaces read access", read,save,true)
        GlassCard {
            Text("GITHUB ACCOUNT",color=GitHubIndigo,style=MaterialTheme.typography.titleMedium)
            Text(if(githubAccount.isBlank()) "Not verified" else "Authenticated as @$githubAccount",color=if(githubAccount.isBlank()) TextSecondary else GoogleEmerald)
            TextButton(onClick=onVerify,enabled=!githubBusy) { Text(if(githubBusy) "VERIFYING…" else "VERIFY TOKEN & LOAD CODESPACES",color=NeonCyan) }
            if(codespaces.isNotEmpty()) { Text("Codespaces returned by GitHub:",color=TextSecondary); codespaces.forEach { Text("• $it",color=TextPrimary,style=MaterialTheme.typography.labelSmall) } }
            Text("GitHub API authentication is separate from SSH access. A token does not expose a Codespace SSH host automatically.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
        }
        CredentialCard("KAGGLE USERNAME", "kaggle_username", "Kaggle username", read,save,false)
        CredentialCard("KAGGLE API KEY", "kaggle_api_key", "API key", read,save,true)
        GlassCard { Text("INTEGRATION BOUNDARIES",color=NeonCyan); Text("This build connects desktops only through the SSH host configured above. GitHub and Kaggle are queried only when you press their real API actions. It does not claim Cloud Shell or Codespaces are connected from their tokens alone, and it does not upload rclone configuration.",color=TextSecondary) }
        GlassCard { Text("CREDENTIAL SECURITY",color=NeonCyan); Text("Secrets are stored with Android encrypted preferences where supported. The emulator fallback is unencrypted and is not safe for production credentials. SSH known_hosts is public trust data; verify its fingerprint independently before saving. No credentials are printed to the UI logs.",color=TextSecondary) }
    }
}

@Composable private fun CredentialCard(title:String,key:String,label:String,read:(String)->String,save:(String,String)->Unit,secret:Boolean) {
    var value by remember(key) { mutableStateOf(read(key)) }
    GlassCard { Text(title,color=NeonCyan,style=MaterialTheme.typography.titleMedium); OutlinedTextField(value,{value=it;save(key,it)},label={Text(label)},modifier=Modifier.fillMaxWidth(),visualTransformation=if(secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,singleLine=key!="rclone_config_base64",minLines=if(key=="rclone_config_base64") 3 else 1) }
}
