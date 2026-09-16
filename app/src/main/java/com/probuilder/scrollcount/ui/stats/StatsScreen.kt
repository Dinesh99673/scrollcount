package com.probuilder.scrollcount.ui.stats

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.probuilder.scrollcount.ui.AppViewModelProvider
import com.probuilder.scrollcount.ui.components.BarEntry
import com.probuilder.scrollcount.ui.components.SectionCard
import com.probuilder.scrollcount.ui.components.SimpleBarChart
import com.probuilder.scrollcount.ui.components.StatBlock
import com.probuilder.scrollcount.ui.share.ShareCard
import com.probuilder.scrollcount.ui.share.ShareCardData
import com.probuilder.scrollcount.ui.share.ShareCardRenderer
import com.probuilder.scrollcount.util.Formatting
import com.probuilder.scrollcount.util.TimeRanges
import kotlinx.coroutines.launch

/**
 * The "what does my week look like" screen: seven daily bars, today by hour,
 * a few summary numbers, and the button that turns all of it into a picture
 * worth posting.
 */
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var building by remember { mutableStateOf(false) }

    val today = TimeRanges.today()
    val weekEntries = state.lastWeek.map { day ->
        BarEntry(
            label = Formatting.shortDayName(day.date),
            value = day.count,
            highlighted = day.date == today,
        )
    }
    val hourEntries = state.hourly.mapIndexed { hour, count ->
        BarEntry(
            label = if (hour % 6 == 0) Formatting.hourLabel(hour) else "",
            value = count,
            highlighted = state.peakHour == hour,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        SectionCard(
            title = "Last 7 days",
            subtitle = "Today is highlighted",
        ) {
            if (weekEntries.isEmpty()) {
                EmptyNote("Nothing counted yet. Come back after a scroll.")
            } else {
                SimpleBarChart(entries = weekEntries, chartHeight = 170.dp)
            }
        }

        SectionCard(
            title = "Summary",
            subtitle = "Across the last 7 days",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatBlock(
                    label = "This week",
                    value = state.weeklyTotal.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatBlock(
                    label = "Daily average",
                    value = state.dailyAverage.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatBlock(
                    label = "Busiest day",
                    value = state.busiestDay?.let { Formatting.shortDayName(it.date) } ?: "-",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Roughly " + Formatting.duration(state.estimatedWeeklySeconds) +
                    " of scrolling, estimated at " + state.secondsPerReel + " seconds a reel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(
            title = "Today by hour",
            subtitle = state.peakHour?.let { "Busiest around " + Formatting.hourLabel(it) }
                ?: "No reels counted today",
        ) {
            SimpleBarChart(
                entries = hourEntries,
                chartHeight = 140.dp,
                showValues = false,
                labelEvery = 6,
            )
        }

        Button(
            onClick = {
                building = true
                scope.launch {
                    preview = ShareCard.preview(state.toShareData())
                    building = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !building,
        ) {
            Icon(imageVector = Icons.Filled.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = if (building) "Making your card..." else "Share my week")
        }

        Spacer(Modifier.height(24.dp))
    }

    val bitmap = preview
    if (bitmap != null) {
        Dialog(onDismissRequest = { preview = null }) {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Your weekly ScrollCount card",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                ShareCardRenderer.WIDTH.toFloat() / ShareCardRenderer.HEIGHT,
                            )
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                    )
                    Button(
                        onClick = {
                            val data = state.toShareData()
                            preview = null
                            scope.launch { ShareCard.share(context, data) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Share it")
                    }
                    OutlinedButton(
                        onClick = { preview = null },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Not now")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Packs the current stats into the shape the share card expects. */
private fun StatsUiState.toShareData(): ShareCardData = ShareCardData(
    weeklyTotal = weeklyTotal,
    dailyAverage = dailyAverage,
    estimatedSeconds = estimatedWeeklySeconds,
    dailyTotals = lastWeek.map { Formatting.shortDayName(it.date) to it.count },
)
