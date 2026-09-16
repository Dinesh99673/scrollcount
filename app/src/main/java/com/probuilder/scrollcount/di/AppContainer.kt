package com.probuilder.scrollcount.di

import android.content.Context
import com.probuilder.scrollcount.data.ReelRepository
import com.probuilder.scrollcount.data.db.ScrollCountDatabase
import com.probuilder.scrollcount.data.settings.SettingsRepository
import com.probuilder.scrollcount.notifications.ReelNotifier

/**
 * Hand-rolled dependency injection.
 *
 * The app is small enough that a library like Hilt would add more concepts than
 * it removes. Everything is created lazily here, once, and both the UI and the
 * accessibility service reach it through ScrollCountApp.container.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: ScrollCountDatabase by lazy { ScrollCountDatabase.get(appContext) }

    val reelRepository: ReelRepository by lazy { ReelRepository(database.reelEventDao()) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    val notifier: ReelNotifier by lazy {
        ReelNotifier(appContext, settingsRepository, reelRepository)
    }
}
