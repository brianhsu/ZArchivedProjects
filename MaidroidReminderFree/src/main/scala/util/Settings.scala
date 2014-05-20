package org.maidroid.reminder2

import android.preference.PreferenceManager
import android.content.Context

class Settings(context: Context)
{
    private val preference = PreferenceManager.getDefaultSharedPreferences(context)

    def enableVoice = preference.getBoolean("voice", false)
    def enableSound = preference.getBoolean("notificationSound", true)
    def enableVibration = preference.getBoolean("notificationVibration", true)
}
