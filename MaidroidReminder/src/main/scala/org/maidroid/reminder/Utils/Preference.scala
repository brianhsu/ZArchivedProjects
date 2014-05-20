/**
 *  Maidroid Reminder
 *  
 *  -------------------------------------------------------------------------
 *  @license
 *
 *  This file is part of Maidroid Reminder
 *
 *  Maidroid Reminder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Maidroid Reminder is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Maidroid Reminder.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *  @author Brian Hsu (brianhsu.hsu [at] gmail.com)
 *  
 */

package org.maidroid.reminder

import android.content.Context
import android.preference.PreferenceManager
import android.widget.Toast

object MaidroidPreference
{
    private val VOICE_ON           = "voiceOn"
    private val IGNORE_SILENT_MODE = "ignoreSilent"
    private val MAID               = "maid"
    private val VOLUME             = "volume"
    private val LONG_TOAST         = "longToast"
    private val SYSTEM_NOTI_SOUND  = "systemNotificationSound"
    private val LOOP_NOTIFICATION  = "loopNotification"
    private val SYNC_REMEMBER      = "syncRemember"
    private val SYNC_USERNAME      = "syncUsername"
    private val SYNC_PASSWORD      = "syncPassword"

}

class MaidroidPreference (context: Context)
{
    import MaidroidPreference._

    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    def maid             = sharedPrefs.getString  (MAID, "")
    def voiceOn          = sharedPrefs.getBoolean (VOICE_ON, true)
    def longToast        = sharedPrefs.getBoolean (LONG_TOAST, false)
    def volume:Float     = sharedPrefs.getInt     (VOLUME, 100)
    def ignoreSilent     = sharedPrefs.getBoolean (IGNORE_SILENT_MODE, false)
    def loopNotification = sharedPrefs.getBoolean (LOOP_NOTIFICATION, false)
    def systemNotificationSound = sharedPrefs.getBoolean (SYSTEM_NOTI_SOUND, false)

    def toastLength = if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT

    def getSyncRemember  = sharedPrefs.getBoolean (SYNC_REMEMBER, false)
    def getSyncUsername  = sharedPrefs.getString (SYNC_USERNAME, "")
    def getSyncPassword  = sharedPrefs.getString (SYNC_PASSWORD, "")

    def setSyncUsername (username: String)  = sharedPrefs.edit.putString (SYNC_USERNAME, username).commit
    def setSyncPassword (password: String)  = sharedPrefs.edit.putString (SYNC_PASSWORD, password).commit
    def setSyncRemember (remember: Boolean) = sharedPrefs.edit.putBoolean (SYNC_REMEMBER, remember).commit

    override def toString = """|Maidroid[voiceOn] = %s
                               |Maidroid[ignoreSilent] = %s
                               |Maidroid[maid] = %s""".
                            format (voiceOn, ignoreSilent, maid).stripMargin
}
