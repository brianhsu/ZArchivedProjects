package org.maidroid.calendar.model

import java.util.Date

trait CalendarDAO {
    def createNewCalendar (title: String, description: Option[String] = None): Calendar
    def getCalendars: List[Calendar]
    def getCalendar (calendarID: Long): Option[Calendar]
    def saveCalendar (newCalendar: Calendar): Unit
    def deleteCalendar (calendar: Calendar): Int

    def createNewEvent (calendar: Calendar, title: String, startTime: Date, isFullDay: Boolean,
                        endTime: Option[Date] = None, description: Option[String] = None,
                        recurrence: Option[String] = None): Event

    def getEvents(calendar: Calendar): List[Event]
    def saveEvent (newEvent: Event): Unit
    def deleteEvent (event: Event): Int

    def getReminders (event: Event): List[Date]
    def addReminder (event: Event, time: Date): Event
    def deleteReminder (event: Event, time: Date): Int
}

