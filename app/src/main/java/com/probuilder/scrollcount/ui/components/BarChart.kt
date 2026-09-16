package com.probuilder.scrollcount.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** One column of the chart. */
data class BarEntry(
    val label: String,
    val value: Int,
    /** Draws this bar in the accent colour, e.g. today or the busiest day. */
    val highlighted: Boolean = false,
)

/**
 * A plain bar chart built from ordinary Compose layout.
 *
 * Written by hand rather than pulled from a chart library: the app only ever
 * draws two very simple charts, and this way there is no third-party API to
 * re-learn every time it releases a new major version.
 */
@Composable
fun SimpleBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    highlightColor: Color = MaterialTheme.colorScheme.secondary,
    chartHeight: Dp = 150.dp,
    showValues: Boolean = true,
    labelEvery: Int = 1,
) {
    if (entries.isEmpty()) return
    val maxValue = entries.maxOf { it.value }

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
        ) {
            // Leave room above the tallest bar for its value label.
            val tallestBar = maxHeight - if (showValues) VALUE_LABEL_SPACE else 0.dp

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
            ) {
                entries.forEach { entry ->
                    val fraction = if (maxValue <= 0) 0f else entry.value.toFloat() / maxValue
                    val barHeight = (tallestBar * fraction).coerceAtLeast(MIN_BAR_HEIGHT)
                    val fill = when {
                        entry.value == 0 -> MaterialTheme.colorScheme.surfaceVariant
                        entry.highlighted -> highlightColor
                        else -> barColor
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        if (showValues) {
                            Text(
                                text = if (entry.value > 0) entry.value.toString() else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(BAR_WIDTH_FRACTION)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(fill),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            entries.forEachIndexed { index, entry ->
                Text(
                    text = if (index % labelEvery == 0) entry.label else "",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

private val MIN_BAR_HEIGHT = 3.dp
private val VALUE_LABEL_SPACE = 20.dp
private const val BAR_WIDTH_FRACTION = 0.58f
