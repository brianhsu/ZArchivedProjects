package org.maidroid.reminder2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.maidroid.reminder2.AndroidDBEvent._

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
        val calendars = Calendar.getCalendarDAO(context).getCalendars

        calendars.foreach(_.getEvents.foreach(_.registerAlarm(context)))
    }
}
