package com.probuilder.scrollcount.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.probuilder.scrollcount.data.DayTotal
import com.probuilder.scrollcount.data.ReelRepository
import com.probuilder.scrollcount.data.settings.SettingsRepository
import com.probuilder.scrollcount.util.Formatting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class StatsUiState(
    val lastWeek: List<DayTotal> = emptyList(),
    val hourly: List<Int> = List(24) { 0 },
    val weeklyTotal: Int = 0,
    val dailyAverage: Int = 0,
    val busiestDay: DayTotal? = null,
    val peakHour: Int? = null,
    val estimatedWeeklySeconds: Int = 0,
    val secondsPerReel: Int = 20,
    val loaded: Boolean = false,
)

/**
 * Turns the raw daily and hourly counts into the handful of numbers the stats
 * screen actually shows. Doing the arithmetic here keeps the screen itself
 * free of logic, which makes both easier to change.
 */
class StatsViewModel(
    reelRepository: ReelRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        reelRepository.observeLastDays(DAYS_SHOWN),
        reelRepository.observeTodayByHour(),
        settingsRepository.settings,
    ) { week, hourly, settings ->
        val weeklyTotal = week.sumOf { it.count }
        val busiest = week.maxByOrNull { it.count }?.takeIf { it.count > 0 }
        val peakHourIndex = hourly.withIndex().maxByOrNull { it.value }
            ?.takeIf { it.value > 0 }?.index

        StatsUiState(
            lastWeek = week,
            hourly = hourly,
            weeklyTotal = weeklyTotal,
            dailyAverage = if (week.isEmpty()) 0 else (weeklyTotal.toFloat() / week.size).roundToInt(),
            busiestDay = busiest,
            peakHour = peakHourIndex,
            estimatedWeeklySeconds = Formatting.estimatedSeconds(weeklyTotal, settings.secondsPerReel),
            secondsPerReel = settings.secondsPerReel,
            loaded = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = StatsUiState(),
    )

    private companion object {
        const val DAYS_SHOWN = 7
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
