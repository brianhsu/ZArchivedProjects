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

import java.util.Calendar

class TriggerTime (private val initTime: Long)
{
    private def this (calendar: Calendar) { this (calendar.getTimeInMillis) }

    private var calendar = {
        val calendar = Calendar.getInstance
        calendar.setTimeInMillis (initTime)

        calendar
    }

    def year         = calendar.get (Calendar.YEAR)
    def month        = calendar.get (Calendar.MONTH)
    def date         = calendar.get (Calendar.DATE)
    def hour         = calendar.get (Calendar.HOUR_OF_DAY)
    def minute       = calendar.get (Calendar.MINUTE)   
    def timeInMillis = calendar.getTimeInMillis

    def setDate (year: Int, month: Int, date: Int): TriggerTime =
    {
        calendar.set (Calendar.YEAR,  year)
        calendar.set (Calendar.MONTH, month)
        calendar.set (Calendar.DATE,  date)

        this
    }

    def setTime (hour: Int, minute: Int): TriggerTime = 
    {
        calendar.set (Calendar.HOUR_OF_DAY,  hour)
        calendar.set (Calendar.MINUTE, minute)

        this
    }

    def nextMonth (): TriggerTime = {
        val newCalendar = Calendar.getInstance

        newCalendar.setTimeInMillis (timeInMillis)

        newCalendar.set (Calendar.DATE, 1)
        newCalendar.add (Calendar.MONTH, 1)
        newCalendar.add (Calendar.DAY_OF_MONTH, -1)

        new TriggerTime (newCalendar).setTime (23, 59)
    }

    def nextWeekend (): TriggerTime = {
        val newCalendar = Calendar.getInstance

        newCalendar.setTimeInMillis (timeInMillis)
        newCalendar.set (Calendar.DAY_OF_WEEK, Calendar.SATURDAY)

        new TriggerTime (newCalendar).setTime (23, 59)
    }

    def toDateString () = { "%04d-%02d-%02d".format (year, month+1, date) }
    def toTimeString () = { "%02d : %02d".format (hour, minute) }

    override def toString = { year + "/" + (month+1) + "/" + date + " " +
                              hour + ":" + minute }

}

