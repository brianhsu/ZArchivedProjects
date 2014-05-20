package org.maidroid.reminder2

import org.maidroid.calendar.model.CalendarDAO

import android.content.Context

object Calendar
{
    private val VERSION = 1

    private var calendarDAO: CalendarDAO = null

    def getCalendarDAO (context: Context) = calendarDAO match {
        case null =>
            val dbHelper = new CalendarDBHelper(context, "main.db", VERSION)
            val calendarDAO = new AndroidDBCalendar(dbHelper)

            calendarDAO.getCalendars.length match {
                case 0 => calendarDAO.createNewCalendar("Main Calendar", Some("Main Calendar"))
                case _ =>
            }

            this.calendarDAO = calendarDAO
            calendarDAO

        case _ => calendarDAO
    }
}

