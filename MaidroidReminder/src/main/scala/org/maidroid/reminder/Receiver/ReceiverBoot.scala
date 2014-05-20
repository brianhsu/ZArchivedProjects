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

import android.app.NotificationManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.util.Log

/**
 *  This class is got called when OS boot completed, and
 *  register alarms in system.
 */
class ReceiverBoot extends BroadcastReceiver
{
    /*=================================================================
     * Android Framework
     *===============================================================*/
    override def onReceive (context: Context, intent: Intent)
    {
        val timestamp = System.currentTimeMillis
        val items = ReminderItem.getAll (context)

        items.filter (_.triggerTime >= timestamp).foreach {
            item: ReminderItem =>
                item.register
                Log.e ("QQQ", "Register Alarm:" + item.title )
        }
    }
}

