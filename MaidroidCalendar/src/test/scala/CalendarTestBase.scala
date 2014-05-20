package org.maidroid.calendar.model.test

import org.scalatest.FeatureSpec
import org.scalatest.matchers.ShouldMatchers

import org.maidroid.calendar.model._
import java.util.Date

abstract class CalendarTestBase extends FeatureSpec with ShouldMatchers {
    def getCalendarDAO: CalendarDAO

    feature ("Operate calendars") {

        val calendarService  = getCalendarDAO

        scenario ("Create calendar") {

            info ("Create calendar TestCalendar1 and TestCalendar2")
            val newCalendar1 = calendarService.createNewCalendar("TestCalendar1", None)
            val newCalendar2 = calendarService.createNewCalendar("TestCalendar2", Some("Calendar Description"))

            info ("Get calendar list and make sure it has two calendars")
            val calendarIDList = calendarService.getCalendars.map(_.calendarID)
            calendarIDList.contains(newCalendar1.calendarID) should be === true
            calendarIDList.contains(newCalendar2.calendarID) should be === true

            info ("Cleanup calendar")
            Calendar.deleteCalendar(newCalendar1)
            Calendar.deleteCalendar(newCalendar2)
        }

        scenario ("Get calendar by CalendarID") {

            info ("Create calendar")
            val newCalendar1 = calendarService.createNewCalendar("TestCalendar1", None)
            val newCalendar2 = calendarService.createNewCalendar("TestCalendar2", Some("Calendar Description"))

            info ("Confirm calendar data")
            calendarService.getCalendars.foreach {calendar =>
                val confirmCalendar = calendarService.getCalendar(calendar.calendarID).get

                confirmCalendar.calendarID  should be === calendar.calendarID
                confirmCalendar.title       should be === calendar.title
                confirmCalendar.description should be === calendar.description
                confirmCalendar.lastUpdated should be === calendar.lastUpdated
            }

            info ("Cleanup calendars")
            Calendar.deleteCalendar(newCalendar1)
            Calendar.deleteCalendar(newCalendar2)
        }

        scenario ("Update calendar information") {

            info ("Create calendar")
            val calendar1 = calendarService.createNewCalendar("TestCalendar1", None)
            val calendar2 = calendarService.createNewCalendar("TestCalendar2", Some("Calendar Description"))

            info ("Update calendar information")
            val updatedCalendar = calendarService.getCalendar(calendar1.calendarID).get.copy (
                title = "NewTestCalendar1",
                description = Some("NewDescription")
            )

            info ("Make sure it doest not update DB before commit")
            calendarService.getCalendar(calendar1.calendarID).get.title should be === "TestCalendar1"
            calendarService.getCalendar(calendar1.calendarID).get.description should be === None

            info ("Commit to Google Calendar")
            updatedCalendar.commit()

            info ("Make sure it update Google Calendar after commit")
            calendarService.getCalendar(calendar1.calendarID).get.title should be === "NewTestCalendar1"
            calendarService.getCalendar(calendar1.calendarID).get.description should be === Some("NewDescription")

            info ("Cleanup calendars")
            Calendar.deleteCalendar(calendar1)
            Calendar.deleteCalendar(calendar2)
        }
    }

    feature ("Operate events in a calendar") {
        val calendarService = getCalendarDAO 

        def initCalendars = {
            val calendar1 = calendarService.createNewCalendar("TestCalendar1", None)
            val calendar2 = calendarService.createNewCalendar("TestCalendar2", Some("Calendar Description"))

            (calendar1, calendar2)
        }

        scenario ("Create a event and added it to calendar") {

            import java.text.SimpleDateFormat
            val dateFormatter = new SimpleDateFormat("yyyyMMdd")

            info ("Create calendars")
            val (calendar1, calendar2) = initCalendars

            info ("Add events")
            val newEvent01 = calendar1.addEvent (
                title = "NewEvent2",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20101201
                                     |DTEND;VALUE=DATE:20101202
                                     |RRULE:FREQ=WEEKLY;BYDAY=We;UNTIL=20110904""".stripMargin)
            )

            val newEvent02 = calendar1.addEvent (
                title = "NewEvent1",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val newEvent03 = calendar2.addEvent (
                title = "NewEvent4",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20101201
                                     |DTEND;VALUE=DATE:20101202
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)

            )

            val newEvent04 = calendar2.addEvent (
                title = "NewEvent3",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )


            info ("Confirm event has been added.")
            calendar1.getEvents.contains(newEvent01) should be === true
            calendar1.getEvents.contains(newEvent02) should be === true
            calendar2.getEvents.contains(newEvent03) should be === true
            calendar2.getEvents.contains(newEvent04) should be === true

            info ("Cleanup calendars")
            Calendar.deleteCalendar(calendar1)
            Calendar.deleteCalendar(calendar2)
        }

        scenario ("Update a event information in a calendar") {

            info ("Create calendars")
            val (calendar1, calendar2) = initCalendars

            info ("Add events")
            val event1 = calendar1.addEvent (
                title = "NewEvent1",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val event2 = calendar1.addEvent (
                title = "NewEvent2",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20100505
                                     |DTEND;VALUE=DATE:20100506
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)

            )

            info ("Modify events")
            val modifiedEvent1 = event1 copy (
                title = "ModifiedEvent1",
                description = Some("DescriptionModified")
            )

            val modifiedEvent2 = event2 copy (
                title = "ModifiedEvent1",
                description = Some("DescriptionModified")
            )
            
            info ("Commit the change")
            modifiedEvent1.commit()
            modifiedEvent2.commit()

            info ("Confirm the SQLite DB has updated")
            calendar1.getEvents.contains(modifiedEvent1) should be === true
            calendar1.getEvents.contains(modifiedEvent2) should be === true

            info ("Cleanup calendars")
            Calendar.deleteCalendar(calendar1)
            Calendar.deleteCalendar(calendar2)
        }

        scenario ("Delete a event in a calendar") {
            info ("Create calendars")
            val (calendar1, calendar2) = initCalendars

            info ("Add events")
            val event1 = calendar1.addEvent (
                title = "NewEvent1",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val event2 = calendar1.addEvent (
                title = "NewEvent2",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20100505
                                     |DTEND;VALUE=DATE:20100506
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)
            )

            val event3 = calendar2.addEvent (
                title = "NewEvent3",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val event4 = calendar2.addEvent (
                title = "NewEvent4",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20100505
                                     |DTEND;VALUE=DATE:20100506
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)

            )
           
            info ("Delete event normally")
            calendar1.deleteEvent(event2)
            calendar2.deleteEvent(event4)

            info ("Confirm the event has been deleted")
            calendar1.getEvents should be === List(event1)
            calendar2.getEvents should be === List(event3)

            info ("Delete event does not exists")
            evaluating {calendar1.deleteEvent(event2)} should produce [Exception]
            evaluating {calendar2.deleteEvent(event4)} should produce [Exception]

            info ("Delete event does not belong to the calendar")
            evaluating {calendar1.deleteEvent(event3)} should produce [Exception]
            evaluating {calendar2.deleteEvent(event1)} should produce [Exception]

            info ("Cleanup calendars")
            Calendar.deleteCalendar(calendar1)
            Calendar.deleteCalendar(calendar2)

        }
    }

    feature ("Operate reminders in a calendar") {

        val calendarService  = getCalendarDAO

        def initTestEnv = {
            val calendar1 = calendarService.createNewCalendar("TestCalendar1", None)
            val calendar2 = calendarService.createNewCalendar("TestCalendar2", Some("Calendar Description"))
            val event01 = calendar1.addEvent (
                title = "NewEvent1",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val event02 = calendar1.addEvent (
                title = "NewEvent2",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20100505
                                     |DTEND;VALUE=DATE:20100506
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)

            )

            val event03 = calendar2.addEvent (
                title = "NewEvent3",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val event04 = calendar2.addEvent (
                title = "NewEvent4",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20100505
                                     |DTEND;VALUE=DATE:20100506
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)

            )

            (calendar1, calendar2, event01, event02, event03, event04)

        }


        scenario ("Add a reminder to a event.") {

            info ("Create calendars and events")
            val (calendar1, calendar2, event01, event02, event03, event04) = initTestEnv

            val event01Before10Mins = new Date(event01.startTime.getTime - 1000 * 60 * 10)
            val event01Before20Mins = new Date(event01.startTime.getTime - 1000 * 60 * 20)

            val event02Before10Mins = new Date(event02.startTime.getTime - 1000 * 60 * 10)
            val event02Before20Mins = new Date(event02.startTime.getTime - 1000 * 60 * 20)

            val event03Before10Mins = new Date(event03.startTime.getTime - 1000 * 60 * 10)
            val event03Before20Mins = new Date(event03.startTime.getTime - 1000 * 60 * 20)

            val event04Before10Mins = new Date(event04.startTime.getTime - 1000 * 60 * 10)
            val event04Before20Mins = new Date(event04.startTime.getTime - 1000 * 60 * 20)

            info ("Add reminders")
            event01.addReminder (event01Before10Mins)
            event01.addReminder (event01Before20Mins)
            event02.addReminder (event02Before10Mins)
            event02.addReminder (event02Before20Mins)
            event03.addReminder (event03Before10Mins)
            event03.addReminder (event03Before20Mins)
            event04.addReminder (event04Before10Mins)
            event04.addReminder (event04Before20Mins)

            info ("Confirm reminders has been added")
            event01.getReminders should be === List(event01Before20Mins, event01Before10Mins)
            event02.getReminders should be === List(event02Before20Mins, event02Before10Mins)
            event03.getReminders should be === List(event03Before20Mins, event03Before10Mins)
            event04.getReminders should be === List(event04Before20Mins, event04Before10Mins)

            info ("Cleanup calendars")
            Calendar.deleteCalendar(calendar1)
            Calendar.deleteCalendar(calendar2)

        }

        scenario ("Delete a reminder to a event.") {
            info ("Create calendars and events")
            val (calendar1, calendar2, event01, event02, event03, event04) = initTestEnv

            val event01Before10Mins = new Date(event01.startTime.getTime - 1000 * 60 * 10)
            val event01Before20Mins = new Date(event01.startTime.getTime - 1000 * 60 * 20)

            val event02Before10Mins = new Date(event02.startTime.getTime - 1000 * 60 * 10)
            val event02Before20Mins = new Date(event02.startTime.getTime - 1000 * 60 * 20)

            val event03Before10Mins = new Date(event03.startTime.getTime - 1000 * 60 * 10)
            val event03Before20Mins = new Date(event03.startTime.getTime - 1000 * 60 * 20)

            val event04Before10Mins = new Date(event04.startTime.getTime - 1000 * 60 * 10)
            val event04Before20Mins = new Date(event04.startTime.getTime - 1000 * 60 * 20)

            info ("Add reminders")
            event01.addReminder (event01Before10Mins)
            event01.addReminder (event01Before20Mins)
            event02.addReminder (event02Before10Mins)
            event02.addReminder (event02Before20Mins)
            event03.addReminder (event03Before10Mins)
            event03.addReminder (event03Before20Mins)
            event04.addReminder (event04Before10Mins)
            event04.addReminder (event04Before20Mins)

            info ("Delete reminders")
            event01.deleteReminder (event01Before10Mins)
            event02.deleteReminder (event02Before20Mins)
            event03.deleteReminder (event03Before10Mins)
            event04.deleteReminder (event04Before10Mins)
            event04.deleteReminder (event04Before20Mins)

            info ("Confirm reminders has been deleted")
            event01.getReminders should be === List(event01Before20Mins)
            event02.getReminders should be === List(event02Before10Mins)
            event03.getReminders should be === List(event03Before20Mins)
            event04.getReminders should be === Nil

            info ("Cleanup calendars")
            Calendar.deleteCalendar(calendar1)
            Calendar.deleteCalendar(calendar2)

        }

        scenario ("Delete a reminder which does not exists.") {
            info ("Create calendars and events")
            val (calendar1, calendar2, event01, event02, event03, event04) = initTestEnv

            val event01Before10Mins = new Date(event01.startTime.getTime - 1000 * 60 * 10)
            val event01Before20Mins = new Date(event01.startTime.getTime - 1000 * 60 * 20)

            val event02Before10Mins = new Date(event02.startTime.getTime - 1000 * 60 * 10)
            val event02Before20Mins = new Date(event02.startTime.getTime - 1000 * 60 * 20)

            val event03Before10Mins = new Date(event03.startTime.getTime - 1000 * 60 * 10)
            val event03Before20Mins = new Date(event03.startTime.getTime - 1000 * 60 * 20)

            val event04Before10Mins = new Date(event04.startTime.getTime - 1000 * 60 * 10)
            val event04Before20Mins = new Date(event04.startTime.getTime - 1000 * 60 * 20)

            info ("Delete reminders that does not exist")
            evaluating {event01.deleteReminder (event01Before10Mins)} should produce[Exception]
            evaluating {event02.deleteReminder (event02Before20Mins)} should produce[Exception]
            evaluating {event03.deleteReminder (event03Before10Mins)} should produce[Exception]
            evaluating {event04.deleteReminder (event04Before10Mins)} should produce[Exception]
            evaluating {event04.deleteReminder (event04Before20Mins)} should produce[Exception]

            info ("Cleanup calendars")
            Calendar.deleteCalendar(calendar1)
            Calendar.deleteCalendar(calendar2)
        }

    }

    feature ("Delete Calendar") {

        val calendarService  = getCalendarDAO

        def initCalendar () = {
            val calendar1 = calendarService.createNewCalendar("TestCalendar1", None)
            val calendar2 = calendarService.createNewCalendar("TestCalendar2", Some("Calendar Description"))
            val event01 = calendar1.addEvent (
                title = "NewEvent1",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val event02 = calendar1.addEvent (
                title = "NewEvent2",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20100505
                                     |DTEND;VALUE=DATE:20100506
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)

            )

            val event03 = calendar2.addEvent (
                title = "NewEvent3",
                startTime = new Date,
                endTime = Some(new Date),
                isFullDay = false,
                description = Some("This is event 01"),
                recurrence = None
            )

            val event04 = calendar2.addEvent (
                title = "NewEvent4",
                startTime = new Date,
                endTime = None,
                isFullDay = true,
                description = Some("This is event 01"),
                recurrence = Some("""|DTSTART;VALUE=DATE:20100505
                                     |DTEND;VALUE=DATE:20100506
                                     |RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20110904""".stripMargin)

            )

            (calendar1, calendar2)
        }

        scenario ("Delete calendars and all its data") {

            info ("Create calendars")
            val (calendar1, calendar2) = initCalendar()

            info ("Delete first calendar")
            Calendar.deleteCalendar(calendar1)
            calendarService.getCalendars.contains(calendar1) should be === false
            
            info ("Delete second calendar")
            Calendar.deleteCalendar(calendar2)
            calendarService.getCalendars.contains(calendar2) should be === false
        }
    }

}
