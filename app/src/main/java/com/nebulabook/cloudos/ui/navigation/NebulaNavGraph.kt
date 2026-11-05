package com.nebulabook.cloudos.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.nebulabook.cloudos.domain.model.CloudEngine
import com.nebulabook.cloudos.ui.components.BootSequenceSheet
import com.nebulabook.cloudos.ui.screens.*
import com.nebulabook.cloudos.ui.theme.*
import com.nebulabook.cloudos.viewmodel.NebulaMainViewModel

private data class Tab(val route:String,val label:String,val icon: androidx.compose.ui.graphics.vector.ImageVector)
@Composable fun NebulaNavGraph(vm:NebulaMainViewModel=viewModel()) {
    val state by vm.state.collectAsState()
    val nav= rememberNavController(); val entry by nav.currentBackStackEntryAsState(); val route=entry?.destination?.route ?: "home"
    val tabs=listOf(Tab("home","Home",Icons.Filled.Dashboard),Tab("apps","App Deck",Icons.Filled.Apps),Tab("render","Render",Icons.Filled.Timelapse),Tab("vault","Vault",Icons.Filled.Key))
    LaunchedEffect(state.connected) { if(state.connected && route!="workspace") { nav.navigate("workspace") { launchSingleTop=true } } }
    var bootSheet by remember { mutableStateOf(false) }
    val snackbarState= remember { SnackbarHostState() }
    LaunchedEffect(route) { if(route=="workspace") bootSheet=false }
    LaunchedEffect(state.notice) { if(state.notice.isNotBlank()) snackbarState.showSnackbar(state.notice) }
    Scaffold(containerColor=ObsidianBg,snackbarHost={ SnackbarHost(snackbarState) },bottomBar={ if(route!="workspace") NavigationBar(containerColor=MidnightSurface) { tabs.forEach { t -> NavigationBarItem(selected=route==t.route,onClick={vm.setTab(tabs.indexOf(t));nav.navigate(t.route){popUpTo("home"){saveState=true};launchSingleTop=true;restoreState=true}},icon={Icon(t.icon,null)},label={Text(t.label)},colors=NavigationBarItemDefaults.colors(selectedIconColor=NeonCyan,selectedTextColor=NeonCyan,indicatorColor=NeonCyan.copy(alpha=.12f))) } } }) { padding ->
        NavHost(navController=nav,startDestination="home",modifier=Modifier.padding(padding)) {
            composable("home") { CommandCenterScreen(state.simulation,state.engine,state.telemetry,vm::toggleSimulation,vm::setEngine,{bootSheet=true;vm.boot()},{nav.navigate("vault")}) }
            composable("apps") { AppDeckScreen(state.simulation,state.installed,state.selectedFilter,vm::setFilter,vm::install) }
            composable("render") { KaggleRenderFarmScreen(state.simulation,state.renderJob,vm::dispatchRender) }
            composable("vault") { VaultSyncScreen(vm::readVault,vm::saveVault,vm::publicKey,vm::generateKey,{vm.notice("Verification is available after saving a token; provider API calls are not run automatically.")}) }
            composable("workspace") { LaptopWorkspaceScreen(state.simulation,state.telemetry,state.engine,state.trackpad,state.ribbon,state.workspaceWindow,vm::toggleTrackpad,vm::toggleRibbon,{vm.closeLid();nav.navigate("home"){popUpTo("home"){inclusive=false};launchSingleTop=true}},vm::chooseWindow,vm::notice) }
        }
    }
    if(bootSheet && route!="workspace") BootSequenceSheet(state.logs) { bootSheet=false }
}
