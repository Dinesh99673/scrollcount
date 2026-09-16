package com.probuilder.scrollcount.ui.home

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.probuilder.scrollcount.service.AccessibilityPermission
import com.probuilder.scrollcount.ui.AppViewModelProvider
import com.probuilder.scrollcount.ui.components.AppDot
import com.probuilder.scrollcount.ui.components.SectionCard
import com.probuilder.scrollcount.ui.components.ServiceOffBanner
import com.probuilder.scrollcount.ui.components.rememberAccessibilityEnabled
import com.probuilder.scrollcount.ui.theme.accentFor
import com.probuilder.scrollcount.util.Formatting
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The screen the app opens on: one big number for today, where it came from,
 * and how close that is to the limit the user set for themselves.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val serviceEnabled = rememberAccessibilityEnabled()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = todayHeading(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!serviceEnabled) {
            ServiceOffBanner(
                onFixClick = { AccessibilityPermission.openAccessibilitySettings(context) },
            )
        }

        TodayHeadline(state = state)

        LimitProgress(state = state)

        SectionCard(
            title = "Where they came from",
            subtitle = "Today only",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.breakdown.forEach { app -> AppRow(app) }
            }
        }

        SectionCard(
            title = "Estimated time spent",
            subtitle = "A rough guess, not a measurement",
        ) {
            Text(
                text = Formatting.duration(state.estimatedSeconds),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Based on about 20 seconds per reel. ScrollCount counts reels, " +
                    "it does not time them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TodayHeadline(state: HomeUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.todayTotal == 0 && state.loaded) {
            Text(
                text = "No reels yet today",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Enjoy the quiet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = state.todayTotal.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (state.todayTotal == 1) "reel today" else "reels today",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LimitProgress(state: HomeUiState) {
    val fraction = if (state.dailyLimit <= 0) {
        0f
    } else {
        (state.todayTotal.toFloat() / state.dailyLimit).coerceIn(0f, 1f)
    }
    val overLimit = state.todayTotal > state.dailyLimit

    SectionCard(
        title = "Daily limit",
        subtitle = "${state.todayTotal} of ${state.dailyLimit} reels",
        trailing = {
            Text(
                text = "${state.percentOfLimit}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (overLimit) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        },
    ) {
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = if (overLimit) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            strokeCap = StrokeCap.Round,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (overLimit) {
                "You are ${state.todayTotal - state.dailyLimit} past your limit."
            } else {
                "${state.dailyLimit - state.todayTotal} left before you hit your limit."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppRow(app: AppBreakdown) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppDot(color = accentFor(app.packageName))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(text = app.displayName, style = MaterialTheme.typography.bodyLarge)
            if (!app.tracked) {
                Text(
                    text = "Not being counted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = app.count.toString(),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

private fun todayHeading(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
    return LocalDate.now().format(formatter)
}
