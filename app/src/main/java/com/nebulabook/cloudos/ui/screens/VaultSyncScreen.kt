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

@Composable fun VaultSyncScreen(read:(String)->String, save:(String,String)->Unit, publicKey:()->String, generate:()->Unit, onVerify:()->Unit) {
    val clip=LocalClipboardManager.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("VAULT & SYNC",style=MaterialTheme.typography.headlineMedium,color=TextPrimary)
        GlassCard { Text("DEVICE SSH KEYPAIR",color=NeonCyan,style=MaterialTheme.typography.titleMedium); val pub=publicKey(); Text(if(pub.isBlank()) "No SSH public key generated" else pub, color=TextSecondary,style=MaterialTheme.typography.labelSmall); Row { Button(onClick=generate) { Text("GENERATE RSA-4096") }; TextButton(onClick={if(pub.isNotBlank()) clip.setText(AnnotatedString(pub))}) { Text("COPY PUBLIC KEY") } } }
        CredentialCard("GOOGLE CLOUD SHELL", "google_oauth_token", "OAuth access token", read,save,true)
        CredentialCard("GITHUB CODESPACES", "github_pat_token", "Personal access token", read,save,true)
        var codespace by remember { mutableStateOf(read("github_codespace_name")) }
        GlassCard { Text("DEFAULT CODESPACE",color=GitHubIndigo); OutlinedTextField(codespace,{codespace=it;save("github_codespace_name",it)},label={Text("Codespace name")},modifier=Modifier.fillMaxWidth()); TextButton(onClick=onVerify) { Text("VERIFY GITHUB API",color=NeonCyan) } }
        CredentialCard("KAGGLE GPU API", "kaggle_username", "Kaggle username", read,save,false)
        CredentialCard("KAGGLE API KEY", "kaggle_api_key", "API key", read,save,true)
        CredentialCard("RCLONE CONFIG", "rclone_config_base64", "Paste rclone.conf or encoded config", read,save,false)
        GlassCard { Text("SECURITY NOTES",color=NeonCyan); Text("Secrets are stored on-device using Android encrypted preferences where available. Emulator fallback uses standard preferences and is not encrypted. Never paste tokens into logs or screenshots. Provider tokens do not create free quotas or automatically establish an SSH host.",color=TextSecondary) }
    }
}

@Composable private fun CredentialCard(title:String,key:String,label:String,read:(String)->String,save:(String,String)->Unit,secret:Boolean) {
    var value by remember(key) { mutableStateOf(read(key)) }
    GlassCard { Text(title,color=NeonCyan,style=MaterialTheme.typography.titleMedium); OutlinedTextField(value,{value=it;save(key,it)},label={Text(label)},modifier=Modifier.fillMaxWidth(),visualTransformation=if(secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,singleLine=key!="rclone_config_base64",minLines=if(key=="rclone_config_base64") 3 else 1) }
}
