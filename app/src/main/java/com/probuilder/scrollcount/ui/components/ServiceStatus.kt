package com.probuilder.scrollcount.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.probuilder.scrollcount.service.AccessibilityPermission
import com.probuilder.scrollcount.service.ScrollCountServiceState

/**
 * Whether counting is actually switched on right now.
 *
 * Two sources are combined because neither is enough on its own: the system
 * setting says the user granted permission (and is re-read every time the app
 * comes back to the foreground, which is how the app notices a change made in
 * Settings), while the service's own flag catches the moment it connects.
 */
@Composable
fun rememberAccessibilityEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val serviceRunning by ScrollCountServiceState.isRunning.collectAsStateWithLifecycle()
    var settingEnabled by remember { mutableStateOf(AccessibilityPermission.isServiceEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingEnabled = AccessibilityPermission.isServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return settingEnabled || serviceRunning
}

/**
 * Shown at the top of the home screen when the accessibility service is off.
 * Without this, a user whose phone silently disabled the service would just see
 * a count stuck at zero and assume the app is broken.
 */
@Composable
fun ServiceOffBanner(
    onFixClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Counting is paused",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "ScrollCount is switched off in your phone's accessibility settings, so no reels are being counted.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = onFixClick) {
                    Text("Turn it back on")
                }
            }
        }
    }
}
