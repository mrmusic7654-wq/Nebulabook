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

@Composable fun VaultSyncScreen(read:(String)->String, save:(String,String)->Unit, publicKey:()->String, generate:()->Unit, onVerify:()->Unit, githubBusy:Boolean, githubAccount:String, kaggleStatus:String, onCheckKaggleStatus:(String)->Unit, cloudShellBusy:Boolean, cloudShellStatus:String, onConnectCloudShell:()->Unit) {
    val clip=LocalClipboardManager.current
    var kernelSlug by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("VAULT & SYNC",style=MaterialTheme.typography.headlineMedium,color=TextPrimary)
        GlassCard { Text("DEVICE SSH KEYPAIR",color=NeonCyan,style=MaterialTheme.typography.titleMedium); val pub=publicKey(); Text(if(pub.isBlank()) "No SSH public key generated" else pub, color=TextSecondary,style=MaterialTheme.typography.labelSmall); Row { Button(onClick=generate) { Text("GENERATE RSA-4096") }; TextButton(onClick={if(pub.isNotBlank()) clip.setText(AnnotatedString(pub))}) { Text("COPY PUBLIC KEY") } } }
        Text("SSH DESKTOP TARGET",color=NeonCyan,style=MaterialTheme.typography.titleMedium)
        CredentialCard("SSH HOST", "ssh_host", "Host name or IP", read,save,false)
        CredentialCard("SSH PORT", "ssh_port", "Port (usually 22)", read,save,false)
        CredentialCard("SSH USERNAME", "ssh_username", "Remote Linux username", read,save,false)
        CredentialCard("SSH KNOWN HOSTS", "ssh_known_hosts", "Verified known_hosts line for this host", read,save,false)
        Text("Generate a key below, add the public key to the remote account’s authorized_keys, then connect. The host fingerprint is required; the app will not disable host verification.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
        GlassCard {
            Text("GOOGLE CLOUD SHELL",color=GoogleEmerald,style=MaterialTheme.typography.titleMedium)
            CredentialCard("GOOGLE OAUTH ACCESS TOKEN", "google_oauth_token", "User OAuth access token", read,save,true)
            Text("Use a user OAuth token authorized for the Cloud Shell API / cloud-platform scope. This app does not perform Google sign-in or refresh expired tokens yet.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            Button(onClick=onConnectCloudShell,enabled=!cloudShellBusy,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=GoogleEmerald,contentColor=ObsidianBg)) { Text(if(cloudShellBusy) "STARTING / CONNECTING…" else "START CLOUD SHELL & CONNECT SSH") }
            if(cloudShellStatus.isNotBlank()) Text(cloudShellStatus,color=TextSecondary,style=MaterialTheme.typography.labelSmall)
        }
        CredentialCard("GITHUB API TOKEN",  "github_pat_token", "Fine-grained token with Codespaces read/write access", read,save,true)
        GlassCard {
            Text("GITHUB ACCOUNT",color=GitHubIndigo,style=MaterialTheme.typography.titleMedium)
            Text(if(githubAccount.isBlank()) "Not verified" else "Authenticated as @$githubAccount",color=if(githubAccount.isBlank()) TextSecondary else GoogleEmerald)
            TextButton(onClick=onVerify,enabled=!githubBusy) { Text(if(githubBusy) "VERIFYING…" else "VERIFY TOKEN & LOAD CODESPACES",color=NeonCyan) }
            Text("After verification, manage real workspaces in the Codespaces tab. API authentication is separate from desktop port access.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
        }
        CredentialCard("KAGGLE USERNAME", "kaggle_username", "Kaggle username", read,save,false)
        CredentialCard("KAGGLE API KEY", "kaggle_api_key", "API key", read,save,true)
        GlassCard {
            Text("KAGGLE · EXISTING KERNEL STATUS",color=KaggleOrange,style=MaterialTheme.typography.titleMedium)
            OutlinedTextField(kernelSlug,{kernelSlug=it},label={Text("Kernel slug")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            Button(onClick={onCheckKaggleStatus(kernelSlug)},enabled=kernelSlug.isNotBlank()) { Text("FETCH LIVE STATUS") }
            if(kaggleStatus.isNotBlank()) Text(kaggleStatus,color=TextSecondary,style=MaterialTheme.typography.labelSmall,fontFamily=androidx.compose.ui.text.font.FontFamily.Monospace)
            Text("Read-only status lookup. No render job is submitted and no GPU quota is estimated.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
        }
        GlassCard { Text("INTEGRATION BOUNDARIES",color=NeonCyan); Text("Cloud Shell can be started through its REST API and then connected over verified SSH. GitHub Codespaces can be listed, resized to an API-reported 2-core machine, started, stopped, and opened in its browser editor; a PAT alone does not create a private desktop port tunnel. Kaggle is status-only, render submission is not implemented, and rclone.conf is not uploaded.",color=TextSecondary) }
        GlassCard { Text("CREDENTIAL SECURITY",color=NeonCyan); Text("Secrets are stored with Android encrypted preferences where supported. The emulator fallback is unencrypted and is not safe for production credentials. SSH known_hosts is public trust data; verify its fingerprint independently before saving. No credentials are printed to the UI logs.",color=TextSecondary) }
    }
}

@Composable private fun CredentialCard(title:String,key:String,label:String,read:(String)->String,save:(String,String)->Unit,secret:Boolean) {
    var value by remember(key) { mutableStateOf(read(key)) }
    GlassCard { Text(title,color=NeonCyan,style=MaterialTheme.typography.titleMedium); OutlinedTextField(value,{value=it;save(key,it)},label={Text(label)},modifier=Modifier.fillMaxWidth(),visualTransformation=if(secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,singleLine=key!="rclone_config_base64",minLines=if(key=="rclone_config_base64") 3 else 1) }
}
