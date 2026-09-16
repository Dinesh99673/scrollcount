package com.probuilder.scrollcount.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.probuilder.scrollcount.BuildConfig
import com.probuilder.scrollcount.data.TrackedApps
import com.probuilder.scrollcount.service.AccessibilityPermission
import com.probuilder.scrollcount.ui.AppViewModelProvider
import com.probuilder.scrollcount.ui.components.AppDot
import com.probuilder.scrollcount.ui.components.SectionCard
import com.probuilder.scrollcount.ui.components.rememberAccessibilityEnabled
import com.probuilder.scrollcount.ui.theme.accentFor
import com.probuilder.scrollcount.util.Links
import kotlin.math.roundToInt

/**
 * Everything the user can change, plus the two things they may need to fix:
 * the accessibility permission and their phone's battery settings.
 */
@Composable
fun SettingsScreen(
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val serviceEnabled = rememberAccessibilityEnabled()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        SectionCard(
            title = "Counting",
            subtitle = if (serviceEnabled) "ScrollCount is on" else "ScrollCount is switched off",
        ) {
            Text(
                text = if (serviceEnabled) {
                    "Reels are being counted in the apps you picked below."
                } else {
                    "Nothing is being counted until you switch ScrollCount on in accessibility settings."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { AccessibilityPermission.openAccessibilitySettings(context) },
            ) {
                Text(if (serviceEnabled) "Open accessibility settings" else "Switch it on")
            }
        }

        SectionCard(
            title = "Apps to count",
            subtitle = "Turn an app off and it is ignored completely",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TrackedApps.all.forEach { app ->
                    SettingSwitchRow(
                        title = app.displayName,
                        subtitle = "Count " + app.itemName,
                        checked = state.settings.isTracked(app.packageName),
                        onCheckedChange = { viewModel.setTracking(app.packageName, it) },
                        leading = { AppDot(color = accentFor(app.packageName)) },
                    )
                }
            }
        }

        SectionCard(
            title = "Daily limit",
            subtitle = state.settings.dailyLimit.toString() + " reels a day",
        ) {
            Slider(
                value = state.settings.dailyLimit.toFloat(),
                onValueChange = { viewModel.setDailyLimit(snapToStep(it)) },
                valueRange = MIN_LIMIT..MAX_LIMIT,
                steps = STEP_COUNT,
            )
            Text(
                text = "You get one nudge at half of this, and one when you reach it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(title = "Notifications") {
            SettingSwitchRow(
                title = "Limit alerts and daily summary",
                subtitle = "At most three notifications a day",
                checked = state.settings.notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
            )
        }

        SectionCard(
            title = "Not counting properly?",
            subtitle = "Some phones stop background apps",
        ) {
            Text(
                text = "Xiaomi, Oppo, Vivo, Realme and Samsung phones often shut ScrollCount " +
                    "down to save battery. Here is how to stop that.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onOpenHelp) {
                Text("Open the fix-it guide")
            }
        }

        SectionCard(
            title = "Your data",
            subtitle = state.totalEverCounted.toString() + " reels counted in total",
        ) {
            Text(
                text = "Everything ScrollCount knows is stored on this phone only. " +
                    "The app has no internet permission at all.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showResetDialog = true }) {
                    Text("Reset all data")
                }
                TextButton(onClick = { openLink(context, Links.PRIVACY_POLICY) }) {
                    Text("Privacy policy")
                }
            }
        }

        if (BuildConfig.DEBUG) {
            SectionCard(
                title = "Developer",
                subtitle = "Debug builds only",
            ) {
                SettingSwitchRow(
                    title = "Log view trees",
                    subtitle = "Dumps view IDs to Logcat under the tag ScrollDebug",
                    checked = state.settings.debugLogging,
                    onCheckedChange = { viewModel.setDebugLogging(it) },
                )
            }
        }

        Text(
            text = "ScrollCount " + BuildConfig.VERSION_NAME + " by ProBuilder",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )

        Spacer(Modifier.height(24.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Delete everything?") },
            text = {
                Text(
                    "This removes every reel ScrollCount has counted, on this phone, " +
                        "for good. Your settings are kept.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) leading()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (leading != null) 12.dp else 0.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun openLink(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
}

/** Keeps the limit slider on round numbers instead of landing on 87. */
private fun snapToStep(raw: Float): Int {
    val stepped = (raw / LIMIT_STEP).roundToInt() * LIMIT_STEP
    return stepped.coerceIn(MIN_LIMIT.toInt(), MAX_LIMIT.toInt())
}

private const val LIMIT_STEP = 10
private const val MIN_LIMIT = 10f
private const val MAX_LIMIT = 500f

/** Discrete stops between the ends of the slider: 10, 20, ... 500. */
private const val STEP_COUNT = 48
