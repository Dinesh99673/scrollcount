# ScrollCount R8 / ProGuard rules for the release build.

# --- AccessibilityService ---------------------------------------------------
# The service is created by the Android system from the name in AndroidManifest,
# so R8 must not rename or remove it.
-keep class com.probuilder.scrollcount.service.ScrollCountService { *; }

# --- Room -------------------------------------------------------------------
# Room generates implementation classes at build time and looks them up by name.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# --- WorkManager ------------------------------------------------------------
# Workers are instantiated reflectively from a class name stored in the DB.
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker { public <init>(...); }

# --- Kotlin coroutines ------------------------------------------------------
-dontwarn kotlinx.coroutines.**

# Keep line numbers so crash reports stay readable, but hide the real file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
