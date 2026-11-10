package com.example.scrollsense

import android.app.Application
import com.example.scrollsense.data.local.ScrollSenseDatabase
import com.example.scrollsense.data.preferences.UserPreferencesRepository
import com.example.scrollsense.data.repository.UsageRepository
import com.example.scrollsense.service.NotificationHelper
import com.example.scrollsense.service.UsageTrackingManager

class ScrollSenseApp : Application() {

    lateinit var database: ScrollSenseDatabase
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var usageTrackingManager: UsageTrackingManager
        private set

    lateinit var usageRepository: UsageRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = ScrollSenseDatabase.getInstance(this)
        preferencesRepository = UserPreferencesRepository(this)
        usageTrackingManager = UsageTrackingManager(this)

        usageRepository = UsageRepository(
            database = database,
            preferencesRepository = preferencesRepository,
            usageTrackingManager = usageTrackingManager
        )

        NotificationHelper.createNotificationChannels(this)
    }
}
