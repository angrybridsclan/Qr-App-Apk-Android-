package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CreateQrScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ScanResultScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@Composable
fun MainApp(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "scan"

    MyApplicationTheme(
        themeMode = settings.themeMode,
        colorIndex = settings.colorIndex
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "QR & Barcode Scanner",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Fast, Secure & Simple",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                        label = { Text("Scan") },
                        selected = currentRoute == "scan",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("scan") {
                                popUpTo("scan") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("nav_drawer_scan")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null) },
                        label = { Text("Create QR") },
                        selected = currentRoute == "create",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("create") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("nav_drawer_create")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("History") },
                        selected = currentRoute == "history",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("history") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("nav_drawer_history")
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("settings") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("nav_drawer_settings")
                    )
                }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "scan",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("scan") {
                    ScanScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToDetail = { scanId ->
                            navController.navigate("result/$scanId")
                        },
                        onNavigateToHistory = {
                            navController.navigate("history")
                        }
                    )
                }

                composable("history") {
                    HistoryScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToDetail = { scanId ->
                            navController.navigate("result/$scanId")
                        }
                    )
                }

                composable("create") {
                    CreateQrScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToDetail = { scanId ->
                            navController.navigate("result/$scanId")
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }

                composable(
                    route = "result/{scanId}",
                    arguments = listOf(navArgument("scanId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val scanId = backStackEntry.arguments?.getLong("scanId") ?: 0L
                    ScanResultScreen(
                        scanId = scanId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
