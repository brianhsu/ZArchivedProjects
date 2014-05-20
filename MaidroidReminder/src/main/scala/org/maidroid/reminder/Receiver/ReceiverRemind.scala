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

/**
 *  This class is got called when a ReminderItem is beeing
 *  notified to user.
 */
class ReceiverRemind extends BroadcastReceiver
{
    /*=================================================================
     * Private Fields
     *===============================================================*/

    /**
     *  Notify user using Notfication Status Bar.
     */
    private def notifiyUser (context: Context, intent: Intent)
    {
        val item    = ReminderItem.get (context, intent.getData).get
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE).
                              asInstanceOf[NotificationManager]

        val notification = Maid.getMaid(context).
                                createNotification(context, item)

        manager.notify (item.id, notification)
    }

    /*=================================================================
     * Android Framework
     *===============================================================*/
    override def onReceive (context: Context, intent: Intent)
    {
        notifiyUser (context, intent)
    }
}

