package org.maidroid.calendar.model

import java.util.Date

object Calendar
{
    def deleteCalendar (calendar: Calendar) {
        calendar.calendarDAO.deleteCalendar(calendar)
    }
}

case class Calendar (private val calendarDAO: CalendarDAO,
                     val calendarID: Long,
                     val calendarURL: String,
                     val title: String,
                     val description: Option[String],
                     val lastUpdated: Date)
{
    def addEvent (title: String, startTime: Date, isFullDay: Boolean, 
                  endTime: Option[Date] = None, 
                  description: Option[String] = None, 
                  recurrence: Option[String] = None) =
    {
        calendarDAO.createNewEvent (this, title, startTime, isFullDay, endTime, description, recurrence)
    }

    def getEvents = calendarDAO.getEvents(this)
    def commit () = calendarDAO.saveCalendar(this)
    def deleteEvent (event: Event) {

        require (event.calendarID == calendarID, "EventID:%d does not exist in calendarID:%d" format(event.eventID, calendarID))

        val updatedRow: Int = calendarDAO.deleteEvent(event)

        require (updatedRow > 0, "There is no eventID:%d exists in calendarID:%d" format(event.eventID, calendarID))
        require (updatedRow < 2, "There are mutliple eventID:%d in Database" format(event.eventID))
    }
}

case class Event (private val calendarDAO: CalendarDAO,
                  val eventID: Long, val calendarID: Long, val eventURL: String, title: String,
                  val startTime: Date, val isFullDay: Boolean, val endTime: Option[Date],
                  val description: Option[String], val recurrence: Option[String])
{
    def commit () = calendarDAO.saveEvent(this)
    def getReminders = calendarDAO.getReminders(this)
    def addReminder (time: Date) = calendarDAO.addReminder(this, time)
    def deleteReminder (time: Date) = {
        val updatedRow = calendarDAO.deleteReminder(this, time)

        require (updatedRow > 0, "There is no time:%s exists in eventID" format(time, eventID))
    }
}
