package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.RolexViewModel
import com.example.ui.screens.AccountCompatibilityScreen
import com.example.ui.screens.ActivateKeyScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.BuyKeyScreen
import com.example.ui.screens.CarromSimulatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.OnyxBlack
import com.example.ui.theme.RolexFastAutoTheme
import com.example.ui.theme.RolexGold
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: RolexViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RolexFastAutoTheme {
                RolexFastAutoApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RolexFastAutoApp(viewModel: RolexViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button: return to HOME if on any secondary screen
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    val navItems = listOf(
        Pair(AppScreen.HOME, Icons.Default.Home),
        Pair(AppScreen.BUY_KEY, Icons.Default.ShoppingCart),
        Pair(AppScreen.ACTIVATE_KEY, Icons.Default.VpnKey),
        Pair(AppScreen.CARROM_SIMULATOR, Icons.Default.SportsEsports),
        Pair(AppScreen.ADMIN_PANEL, Icons.Default.Security),
        Pair(AppScreen.SETTINGS, Icons.Default.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OnyxBlack,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .background(DarkSurface)
                    .navigationBarsPadding()
                    .testTag("rolex_bottom_nav"),
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { (screen, icon) ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(screen) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnyxBlack,
                            selectedTextColor = RolexGold,
                            indicatorColor = RolexGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                AppScreen.BUY_KEY -> BuyKeyScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                AppScreen.ACTIVATE_KEY -> ActivateKeyScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                AppScreen.CARROM_SIMULATOR -> CarromSimulatorScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                AppScreen.ADMIN_PANEL -> AdminPanelScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                AppScreen.ACCOUNT_COMPATIBILITY -> AccountCompatibilityScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    }
}
