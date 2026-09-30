package com.chandeh.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandeh.app.data.Prefs
import com.chandeh.app.data.UpdateChecker
import com.chandeh.app.ui.PricesViewModel
import com.chandeh.app.ui.screens.ConverterScreen
import com.chandeh.app.ui.screens.PriceListScreen
import com.chandeh.app.ui.screens.SettingsScreen
import com.chandeh.app.ui.theme.AppTheme
import com.chandeh.app.ui.theme.NerkhCheckTheme
import com.chandeh.app.ui.theme.appThemeById
import com.chandeh.app.widget.WidgetUpdateWorker

class MainActivity : ComponentActivity() {

    private val vm: PricesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // به‌روزرسانی دوره‌ای ویجت در پس‌زمینه
        WidgetUpdateWorker.schedule(this)
        // تمام‌صفحه: محتوا زیر نوار وضعیت و ناوبری کشیده می‌شود
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            val prefs = remember { getSharedPreferences(Prefs.NAME, MODE_PRIVATE) }
            var theme by remember {
                mutableStateOf(appThemeById(prefs.getString(Prefs.KEY_THEME, null)))
            }
            var brsKey by remember {
                mutableStateOf(prefs.getString(Prefs.KEY_BRS_API, "").orEmpty())
            }
            NerkhCheckTheme(appTheme = theme) {
                // کل رابط راست‌چین و فارسی
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // موتور بررسی آپدیت: روزی یک‌بار ریلیزهای گیت‌هاب چک می‌شود؛
                    // اگر نسخه‌ی جدیدی باشد، همین‌جا داخل برنامه آلارم می‌دهد
                    val ctx = LocalContext.current
                    var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
                    LaunchedEffect(Unit) {
                        updateInfo = UpdateChecker.check(prefs)
                    }
                    // Surface در ریشه تا رنگ محتوای پیش‌فرض (onSurface) به همه‌ی
                    // متن‌ها برسد؛ وگرنه متن‌های بدون رنگ صریح مشکی می‌مانند
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(
                            vm = vm,
                            theme = theme,
                            onThemeChange = {
                                theme = it
                                prefs.edit().putString(Prefs.KEY_THEME, it.id).apply()
                            },
                            brsKey = brsKey,
                            onBrsKeyChange = {
                                brsKey = it
                                prefs.edit().putString(Prefs.KEY_BRS_API, it).apply()
                                vm.refresh()
                            }
                        )
                    }
                    val info = updateInfo
                    if (info != null) {
                        AlertDialog(
                            onDismissRequest = {
                                UpdateChecker.dismiss(prefs, info.version)
                                updateInfo = null
                            },
                            title = { Text("نسخه‌ی جدید منتشر شد") },
                            text = {
                                Text("نسخه‌ی ${info.version} از NerkhCheck در گیت‌هاب منتشر شده است. برای دریافت آن به صفحه‌ی ریلیز بروید.")
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    UpdateChecker.dismiss(prefs, info.version)
                                    updateInfo = null
                                    openReleasePage(info.url, ctx)
                                }) { Text("مشاهده و دانلود") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    UpdateChecker.dismiss(prefs, info.version)
                                    updateInfo = null
                                }) { Text("بعداً") }
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
    onThemeChange: (AppTheme) -> Unit,
    brsKey: String,
    onBrsKeyChange: (String) -> Unit
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
                    lastFetchAt = state.lastFetchAt,
                    hasStale = state.hasStale,
                    onRefresh = vm::refresh
                )
                1 -> ConverterScreen(items = state.items)
                else -> SettingsScreen(
                    theme = theme,
                    onThemeChange = onThemeChange,
                    brsKey = brsKey,
                    onBrsKeyChange = onBrsKeyChange
                )
            }
        }
    }
}

/** باز کردن صفحه‌ی ریلیز گیت‌هاب در مرورگر */
private fun openReleasePage(url: String, ctx: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(intent)
}
