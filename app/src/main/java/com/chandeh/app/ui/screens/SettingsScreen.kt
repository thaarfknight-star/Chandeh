package com.chandeh.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chandeh.app.BuildConfig
import com.chandeh.app.ui.theme.AppTheme

private fun openUrl(ctx: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(intent)
}

/** باز کردن بروشور معرفی برنامه (intro.pdf داخل assets) با نمایشگر PDF گوشی */
private fun openIntroPdf(ctx: Context) {
    try {
        val file = java.io.File(ctx.cacheDir, "intro.pdf")
        if (!file.exists()) {
            ctx.assets.open("intro.pdf").use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        val uri = androidx.core.content.FileProvider.getUriForFile(
            ctx, "${ctx.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(Intent.createChooser(intent, "معرفی NerkhCheck"))
    } catch (_: Exception) {
    }
}

@Composable
fun SettingsScreen(
    theme: AppTheme,
    allThemes: List<AppTheme>,
    onThemeChange: (AppTheme) -> Unit,
    brsKey: String,
    onBrsKeyChange: (String) -> Unit,
    updateStatus: String?,
    checkingUpdate: Boolean,
    onCheckUpdate: () -> Unit
) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "تنظیمات",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp
        )

        SettingsSection(title = "داده‌ها و اینترنت ملی", icon = Icons.Filled.CurrencyExchange) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "روی اینترنت ملی، قیمت طلا و سکه به‌صورت زنده دریافت می‌شود. برای قیمت لحظه‌ای دلار و ارزها هم، کلید رایگان BRS را وارد کنید؛ در غیر این صورت آخرین قیمت ذخیره‌شده نمایش داده می‌شود.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                var draft by remember(brsKey) { mutableStateOf(brsKey) }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("کلید BRS API (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onBrsKeyChange(draft.trim()) }) {
                        Text("ذخیره")
                    }
                    OutlinedButton(
                        onClick = {
                            openUrl(
                                ctx,
                                "https://brsapi.ir/tsetmc-exchange-free-bourse-api-key-request/"
                            )
                        }
                    ) {
                        Text("دریافت کلید رایگان")
                    }
                }
                if (brsKey.isNotBlank()) {
                    Text(
                        "✓ کلید ذخیره شده و برای به‌روزرسانی بعدی استفاده می‌شود",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        SettingsSection(title = "تم رنگی", icon = Icons.Filled.Palette) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allThemes.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { t ->
                            ThemeCard(
                                theme = t,
                                selected = t.id == theme.id,
                                onClick = { onThemeChange(t) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // اگر ردیف ناقص بود، فضای خالی را پر کن
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        SettingsSection(title = "به‌روزرسانی", icon = Icons.Filled.SystemUpdate) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "فایل‌های آپدیت (مثل تم‌های جدید) به‌صورت خودکار دریافت و همان‌لحظه اعمال می‌شوند. برای نسخه‌های جدید کد هم، خود برنامه دانلود و نصب می‌کند.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onCheckUpdate,
                    enabled = !checkingUpdate
                ) {
                    Text(if (checkingUpdate) "در حال بررسی..." else "بررسی آپدیت")
                }
                if (updateStatus != null) {
                    Text(
                        updateStatus,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        SettingsSection(title = "درباره‌ی NerkhCheck", icon = Icons.Filled.Info) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoRow(
                    icon = Icons.Filled.Info,
                    title = "نسخه‌ی برنامه",
                    value = BuildConfig.VERSION_NAME
                )
                InfoRow(
                    icon = Icons.Filled.Description,
                    title = "معرفی برنامه",
                    value = "بروشور NerkhCheck",
                    onClick = { openIntroPdf(ctx) }
                )
                InfoRow(
                    icon = Icons.Filled.Code,
                    title = "گیت‌هاب",
                    value = "thaarfknight-star/NerkhCheck",
                    onClick = { openUrl(ctx, "https://github.com/thaarfknight-star/NerkhCheck") }
                )
                InfoRow(
                    icon = Icons.Filled.Send,
                    title = "تلگرام",
                    value = "@tha_arf",
                    subtitle = "انتقاد و پیشنهاد",
                    onClick = { openUrl(ctx, "https://t.me/tha_arf") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(Modifier.padding(14.dp)) { content() }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(theme.accent, theme.accent2)
                        )
                    )
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = theme.accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Text(theme.nameFa, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
