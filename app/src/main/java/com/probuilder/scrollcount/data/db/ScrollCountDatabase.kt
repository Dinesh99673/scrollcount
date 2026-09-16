package com.probuilder.scrollcount.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's only database. It lives in the app's private storage, is never
 * backed up to the cloud (see AndroidManifest) and holds nothing but counts.
 */
@Database(entities = [ReelEvent::class], version = 1, exportSchema = true)
abstract class ScrollCountDatabase : RoomDatabase() {

    abstract fun reelEventDao(): ReelEventDao

    companion object {
        private const val DATABASE_NAME = "scrollcount.db"

        @Volatile
        private var instance: ScrollCountDatabase? = null

        /**
         * Returns the single shared database. The accessibility service and the
         * UI run in the same process but are created independently, so this
         * double-checked lock makes sure they share one connection.
         */
        fun get(context: Context): ScrollCountDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScrollCountDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
    }
}
