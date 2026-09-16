package com.probuilder.scrollcount.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.probuilder.scrollcount.service.AccessibilityPermission
import com.probuilder.scrollcount.ui.components.SectionCard

/**
 * The "why did my count stop" guide.
 *
 * Several phone brands kill background services far more aggressively than
 * stock Android does, and ScrollCount looks broken when that happens. Rather
 * than ask for extra permissions to fight it, the app explains the fix and
 * links to the right settings screen.
 */
@Composable
fun BatteryHelpScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = "If your count stops going up, your phone is probably shutting " +
                "ScrollCount down in the background. Two settings usually fix it.",
            style = MaterialTheme.typography.bodyMedium,
        )

        SectionCard(
            title = "1. Let it run in the background",
            subtitle = "Battery optimisation",
        ) {
            Text(
                text = "Find ScrollCount in the battery list and set it to " +
                    "Unrestricted, or add it to the allowed list.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { AccessibilityPermission.openBatterySettings(context) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open battery settings")
            }
        }

        SectionCard(
            title = "2. Lock it in recents",
            subtitle = "Xiaomi, Oppo, Vivo, Realme, OnePlus",
        ) {
            Text(
                text = BRAND_STEPS,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { AccessibilityPermission.openAppSettings(context) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open app settings")
            }
        }

        SectionCard(
            title = "3. Check the switch is still on",
            subtitle = "Accessibility settings",
        ) {
            Text(
                text = "A system update or a battery saver can switch accessibility " +
                    "services off without telling you. It is worth a quick look.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { AccessibilityPermission.openAccessibilitySettings(context) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open accessibility settings")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private const val BRAND_STEPS =
    "Xiaomi and Redmi: open Recents, swipe down on the ScrollCount card, tap the " +
        "padlock. Then Settings, Apps, ScrollCount, and set Autostart on and " +
        "Battery saver to No restrictions.\n\n" +
        "Oppo, Realme and OnePlus: Settings, Battery, App battery management, " +
        "ScrollCount, then Allow background running.\n\n" +
        "Vivo: Settings, Battery, High background power consumption, and allow " +
        "ScrollCount.\n\n" +
        "Samsung: Settings, Apps, ScrollCount, Battery, then Unrestricted. Also " +
        "check that ScrollCount is not in Settings, Battery, Background usage " +
        "limits, Sleeping apps."
