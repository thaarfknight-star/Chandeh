package com.chandeh.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandeh.app.ui.PricesViewModel
import com.chandeh.app.ui.screens.ConverterScreen
import com.chandeh.app.ui.screens.PriceListScreen
import com.chandeh.app.ui.screens.SettingsScreen
import com.chandeh.app.ui.theme.AppTheme
import com.chandeh.app.ui.theme.NerkhCheckTheme
import com.chandeh.app.ui.theme.appThemeById

private const val PREFS = "nerkhcheck_prefs"
private const val KEY_THEME = "theme_id"

class MainActivity : ComponentActivity() {

    private val vm: PricesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // تمام‌صفحه: محتوا زیر نوار وضعیت و ناوبری کشیده می‌شود
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            val prefs = remember { getSharedPreferences(PREFS, MODE_PRIVATE) }
            var theme by remember {
                mutableStateOf(appThemeById(prefs.getString(KEY_THEME, null)))
            }
            NerkhCheckTheme(appTheme = theme) {
                // کل رابط راست‌چین و فارسی
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // پس‌زمینه‌ی سراسری تا زیر نوارهای سیستم هم رنگی باشد
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        MainScreen(
                            vm = vm,
                            theme = theme,
                            onThemeChange = {
                                theme = it
                                prefs.edit().putString(KEY_THEME, it.id).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    vm: PricesViewModel,
    theme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("قیمت‌ها") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.CurrencyExchange, contentDescription = null) },
                    label = { Text("تبدیل") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("تنظیمات") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> PriceListScreen(
                    items = state.items,
                    isLoading = state.isLoading,
                    error = state.error,
                    onRefresh = vm::refresh
                )
                1 -> ConverterScreen(items = state.items)
                else -> SettingsScreen(
                    theme = theme,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}
