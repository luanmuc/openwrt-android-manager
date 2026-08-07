package org.openwrt.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.openwrt.manager.ui.addrouter.AddRouterScreen
import org.openwrt.manager.ui.advanced.AdvancedScreen
import org.openwrt.manager.ui.devices.DevicesScreen
import org.openwrt.manager.ui.devices.OnlineDevicesScreen
import org.openwrt.manager.ui.ddns.DdnsScreen
import org.openwrt.manager.ui.firewall.FirewallScreen
import org.openwrt.manager.ui.home.HomeScreen
import org.openwrt.manager.ui.network.NetworkScreen
import org.openwrt.manager.ui.plugins.PluginsScreen
import org.openwrt.manager.ui.profile.ProfileScreen
import org.openwrt.manager.ui.system.SystemScreen
import org.openwrt.manager.ui.theme.OpenWrtManagerTheme
import org.openwrt.manager.ui.wifi.WifiScreen

/**
 * 底部导航项
 */
sealed class Screen(val route: String, val label: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    data object Devices : Screen("devices", R.string.nav_devices, Icons.Default.Devices)
    data object Plugins : Screen("plugins", R.string.nav_plugins, Icons.Default.Extension)
    data object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    data object AddRouter : Screen("add_router", R.string.add_router_title, Icons.Default.Add)
    data object OnlineDevices : Screen("online_devices", 0, Icons.Default.Devices)
    data object System : Screen("system", 0, Icons.Default.Devices)
    data object Network : Screen("network", 0, Icons.Default.Devices)
    data object Wifi : Screen("wifi", 0, Icons.Default.Devices)
    data object Firewall : Screen("firewall", 0, Icons.Default.Devices)
    data object Ddns : Screen("ddns", 0, Icons.Default.Devices)
    data object Advanced : Screen("advanced", 0, Icons.Default.Devices)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Devices,
    Screen.Plugins,
    Screen.Profile
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenWrtManagerTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.label)
                            )
                        },
                        label = { Text(stringResource(screen.label)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddRouter = { navController.navigate(Screen.AddRouter.route) },
                    onNavigateToDevices = { navController.navigate(Screen.OnlineDevices.route) }
                )
            }
            composable(Screen.Devices.route) {
                DevicesScreen(
                    onAddRouter = { navController.navigate(Screen.AddRouter.route) }
                )
            }
            composable(Screen.Plugins.route) {
                PluginsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSystem = { navController.navigate(Screen.System.route) },
                    onNavigateToNetwork = { navController.navigate(Screen.Network.route) },
                    onNavigateToWifi = { navController.navigate(Screen.Wifi.route) },
                    onNavigateToFirewall = { navController.navigate(Screen.Firewall.route) },
                    onNavigateToDdns = { navController.navigate(Screen.Ddns.route) },
                    onNavigateToAdvanced = { navController.navigate(Screen.Advanced.route) }
                )
            }
            composable(Screen.AddRouter.route) {
                AddRouterScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.popBackStack()
                        navController.navigate(Screen.Home.route)
                    }
                )
            }
            composable(Screen.OnlineDevices.route) {
                OnlineDevicesScreen()
            }
            composable(Screen.System.route) {
                SystemScreen()
            }
            composable(Screen.Network.route) {
                NetworkScreen()
            }
            composable(Screen.Wifi.route) {
                WifiScreen()
            }
            composable(Screen.Firewall.route) {
                FirewallScreen()
            }
            composable(Screen.Ddns.route) {
                DdnsScreen()
            }
            composable(Screen.Advanced.route) {
                AdvancedScreen()
            }
        }
    }
}
