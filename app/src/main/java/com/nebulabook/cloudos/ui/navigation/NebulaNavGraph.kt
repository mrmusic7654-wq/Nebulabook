package com.nebulabook.cloudos.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.nebulabook.cloudos.ui.components.BootSequenceSheet
import com.nebulabook.cloudos.ui.screens.*
import com.nebulabook.cloudos.ui.theme.*
import com.nebulabook.cloudos.viewmodel.NebulaMainViewModel

private data class Tab(val route:String,val label:String,val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable fun NebulaNavGraph(vm:NebulaMainViewModel=viewModel()) {
    val state by vm.state.collectAsState()
    val nav=rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route=entry?.destination?.route ?: "home"
    val tabs=listOf(Tab("home","Home",Icons.Filled.Dashboard),Tab("apps","App Deck",Icons.Filled.Apps),Tab("render","Render",Icons.Filled.Timelapse),Tab("vault","Vault",Icons.Filled.Key))
    LaunchedEffect(state.connected,route) {
        if(state.connected && route!="workspace") nav.navigate("workspace") { launchSingleTop=true }
        else if(!state.connected && route=="workspace") nav.navigate("home") { popUpTo("home") { inclusive=false }; launchSingleTop=true }
    }
    var bootSheet by remember { mutableStateOf(false) }
    val snackbarState=remember { SnackbarHostState() }
    val context=LocalContext.current
    val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if(granted) { bootSheet=true; vm.boot() } else vm.notice("Notification permission is required to keep an active SSH desktop session visible and managed.")
    }
    val requestAndConnect:()->Unit = {
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else { bootSheet=true; vm.boot() }
    }
    LaunchedEffect(route) { if(route=="workspace") bootSheet=false }
    LaunchedEffect(state.notice) { if(state.notice.isNotBlank()) snackbarState.showSnackbar(state.notice) }
    Scaffold(containerColor=ObsidianBg,snackbarHost={SnackbarHost(snackbarState)},bottomBar={
        if(route!="workspace") NavigationBar(containerColor=MidnightSurface) {
            tabs.forEach { tab -> NavigationBarItem(
                selected=route==tab.route,
                onClick={nav.navigate(tab.route){popUpTo("home"){saveState=true};launchSingleTop=true;restoreState=true}},
                icon={Icon(tab.icon,null)},label={Text(tab.label)},
                colors=NavigationBarItemDefaults.colors(selectedIconColor=NeonCyan,selectedTextColor=NeonCyan,indicatorColor=NeonCyan.copy(alpha=.12f))
            ) }
        }
    }) { padding ->
        NavHost(navController=nav,startDestination="home",modifier=Modifier.padding(padding)) {
            composable("home") {
                CommandCenterScreen(state.connected,state.booting,state.sshHost,state.telemetry,state.githubAccount,state.logs,
                    requestAndConnect,vm::closeLid,{nav.navigate("vault")})
            }
            composable("apps") {
                AppDeckScreen(state.connected,state.installed,state.activeInstall,state.installLogs,state.selectedFilter,vm::setFilter,vm::install)
            }
            composable("render") { KaggleRenderFarmScreen(state.kaggleStatus,vm::checkKaggleStatus) }
            composable("vault") {
                VaultSyncScreen(vm::readVault,vm::saveVault,vm::publicKey,vm::generateKey,vm::verifyGitHub,state.githubBusy,state.githubAccount,state.codespaces)
            }
            composable("workspace") {
                LaptopWorkspaceScreen(state.telemetry,state.sshHost,state.sessionStartedAt,state.trackpad,state.ribbon,vm::toggleTrackpad,vm::toggleRibbon,
                    vm::closeLid)
            }
        }
    }
    if(bootSheet && route!="workspace") BootSequenceSheet(state.logs) { bootSheet=false }
}
