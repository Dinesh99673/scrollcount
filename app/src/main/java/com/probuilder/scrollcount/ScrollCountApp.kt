package com.probuilder.scrollcount

import android.app.Application
import com.probuilder.scrollcount.di.AppContainer
import com.probuilder.scrollcount.work.SummaryScheduler

/**
 * Application entry point.
 *
 * Creates the shared AppContainer and does the two bits of one-time setup the
 * app needs: registering its notification channels and booking the evening
 * summary job.
 */
class ScrollCountApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifier.ensureChannels()
        SummaryScheduler.schedule(this)
    }
}
