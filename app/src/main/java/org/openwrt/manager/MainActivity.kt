package org.openwrt.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import org.openwrt.manager.ui.devices.DevicesScreen
import org.openwrt.manager.ui.home.HomeScreen
import org.openwrt.manager.ui.plugins.PluginsScreen
import org.openwrt.manager.ui.profile.ProfileScreen
import org.openwrt.manager.ui.theme.OpenWrtManagerTheme

/**
 * 底部导航项
 */
sealed class Screen(val route: String, val label: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    data object Devices : Screen("devices", R.string.nav_devices, Icons.Default.Devices)
    data object Plugins : Screen("plugins", R.string.nav_plugins, Icons.Default.Extension)
    data object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    data object AddRouter : Screen("add_router", R.string.add_router_title, Icons.Default.Add)
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
                    onNavigateToDevices = { navController.navigate(Screen.Devices.route) }
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
                ProfileScreen()
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
        }
    }
}
