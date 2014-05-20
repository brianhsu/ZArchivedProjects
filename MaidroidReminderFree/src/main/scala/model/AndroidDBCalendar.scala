package org.maidroid.reminder2

import android.content.ContentValues
import android.content.Context

import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor

import java.util.Date

import org.maidroid.calendar.model._

class CalendarDBHelper(context: Context, dbName: String, version: Int) extends 
      SQLiteOpenHelper(context, dbName, null, version)
{
    private object CreateTableSQL {
        val createCalendarTable = """
            CREATE TABLE IF NOT EXISTS Calendar (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                title       TEXT NOT NULL,
                description TEXT,
                lastUpdated DATETIME NOT NULL
            );
        """

        val createEventTable = """
            CREATE TABLE IF NOT EXISTS Event (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                calendarID  INTEGER NOT NULL,
                eventURL    TEXT NOT NULL,
                title       TEXT NOT NULL,
                startTime   DATETIME NOT NULL,
                isFullDay   BOOLEAN NOT NULL,
                endTime     DATETIME,
                description TEXT,
                recurrence  TEXT
            );
        """
        val createReminderTable = """
            CREATE TABLE IF NOT EXISTS Reminder (
                eventID     INTEGER NOT NULL,
                time        DATETIME
            );
        """
    }

    def onCreate (db: SQLiteDatabase) {
        db.execSQL (CreateTableSQL.createCalendarTable)
        db.execSQL (CreateTableSQL.createEventTable)
        db.execSQL (CreateTableSQL.createReminderTable)
    }

    def onUpgrade (db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Calendar")
        db.execSQL("DROP TABLE IF EXISTS Event")
        db.execSQL("DROP TABLE IF EXISTS Reminder")
    }
}

class AndroidDBCalendar(dbHelper: CalendarDBHelper) extends CalendarDAO
{
    import CursorSeq._
    type JLong = java.lang.Long

    val database = dbHelper.getWritableDatabase

    def createNewCalendar (title: String, description: Option[String] = None) = {

        val contentValues = new ContentValues()
        contentValues.put("title", title)
        contentValues.put("description", description getOrElse null)
        contentValues.put("lastUpdated", (new Date).getTime: JLong)

        val rowID = database.insert ("Calendar", null, contentValues)
        getCalendar(rowID).get
    }

    private def calendarFromCursor (cursor: Cursor) =
    {
        val calendarID = cursor.getLong(0)
        val title = cursor.getString(1)
        val description = cursor.getString(2) match {
            case null => None
            case text => Some(text)
        }
        val lastUpdated = new Date(cursor.getLong(3))
        val calendarURL: String = "mcalendar://androidDB/%d" format(calendarID)

        new Calendar(this, calendarID, calendarURL, title, description, lastUpdated)
    }

    private def eventsFromCursor (cursor: Cursor) = {
        val eventID    = cursor.getLong(0)
        val calendarID = cursor.getLong(1)
        val eventURL   = cursor.getString(2)
        val title      = cursor.getString(3)
        val startTime  = new Date(cursor.getLong(4))
        val isFullDay  = cursor.getInt(5) match {case 1 => true; case _ => false}
        val endTime    = cursor.getString(6) match {case null => None; 
                                                    case dateTime => Some(new Date(dateTime.toLong))}
        val description = cursor.getString(7) match {case null => None; case text => Some(text)}
        val recurrence  = cursor.getString(8) match {case null => None; case text => Some(text)}

        Event(this, eventID, calendarID, eventURL, title, startTime, isFullDay, 
              endTime, description, recurrence)
    }

    def getCalendar(calendarID: Long): Option[Calendar] = {
        val cursor = database.query("Calendar", null, "id=?", Array(calendarID.toString), null, null, null)

        usingCursor(cursor) {
            cursor.map(calendarFromCursor) match {
                case Nil => None
                case calendar :: xs => Some(calendar)
            }
        }
    }

    def getCalendars(): List[Calendar] = {
        val cursor = database.query("Calendar", null, null, null, null, null, "id", null)

        usingCursor(cursor) {
            cursor.map(calendarFromCursor).toList
        }
    }

    def saveCalendar (newCalendar: Calendar) {
        val updatedValues = new ContentValues()
        updatedValues.put ("title", newCalendar.title)
        updatedValues.put ("description", newCalendar.description getOrElse null)
        updatedValues.put ("lastUpdated", (new Date).getTime: JLong)

        database.update ("Calendar", updatedValues, "id=?", Array(newCalendar.calendarID.toString))
    }

    def createNewEvent (calendar: Calendar, title: String, startTime: Date, isFullDay: Boolean,
                        endTime: Option[Date] = None,
                        description: Option[String] = None,
                        recurrence: Option[String] = None) = {

        val insertValue = new ContentValues()

        insertValue.put ("calendarID", calendar.calendarID: JLong)
        insertValue.put ("eventURL", "mevent://androidDB/%s/%s" format(calendar.calendarID, (new Date).getTime))
        insertValue.put ("title", title)
        insertValue.put ("startTime", startTime.getTime: JLong)
        insertValue.put ("isFullDay", if (isFullDay) 1:JLong else 0:JLong)
        insertValue.put ("endTime", endTime match {case Some(dateTime) => dateTime.getTime.toString; 
                                                   case None => null})
        insertValue.put ("description", description getOrElse null)
        insertValue.put ("recurrence", recurrence getOrElse null)

        val eventID = database.insert ("Event", null, insertValue)
        getEvent(eventID).get
    }

    def getEvents (calendar: Calendar): List[Event] = {
        val cursor = database.query("Event", null, "calendarID=?", Array(calendar.calendarID.toString), 
                                    null, null, "startTime")
        
        usingCursor(cursor) {
            cursor.map(eventsFromCursor).toList
        }
    }

    def getEvent(eventID: Long): Option[Event] = {
        val cursor = database.query("Event", null, "id=?", Array(eventID.toString), null, null, null)

        usingCursor(cursor) {
            cursor.map(eventsFromCursor) match {
                case Nil => None
                case event :: xs => Some(event)
            }
        }
    }

    def saveEvent (newEvent: Event) {

        val updatedValues = new ContentValues()

        updatedValues.put ("calendarID", newEvent.calendarID: JLong)
        updatedValues.put ("eventURL", newEvent.eventURL)
        updatedValues.put ("title", newEvent.title)
        updatedValues.put ("startTime", newEvent.startTime.getTime: JLong)
        updatedValues.put ("isFullDay", if (newEvent.isFullDay) 1:JLong else 0:JLong)
        updatedValues.put ("endTime", newEvent.endTime match {case Some(dateTime) => dateTime.getTime.toString; 
                                                              case None => null})
        updatedValues.put ("description", newEvent.description getOrElse null)
        updatedValues.put ("recurrence", newEvent.recurrence getOrElse null)

        database.update ("Event", updatedValues, "id=?", Array(newEvent.eventID.toString))
    }

    def deleteEvent(event: Event): Int = {
        event.getReminders.foreach {reminder => event.deleteReminder(reminder)}
        database.delete("Event", "id=?", Array(event.eventID.toString))
    }

    def deleteReminder(event: Event, time: Date): Int = {
        database.delete("Reminder", "eventID=? AND time=?", 
                        Array(event.eventID.toString, time.getTime.toString))
    }

    def deleteCalendar (calendar: Calendar) = {
        calendar.getEvents.foreach(event => calendar.deleteEvent(event))
        database.delete("Calendar", "id=?", Array(calendar.calendarID.toString))
    }

    def getReminders(event: Event): List[Date] = {
        val cursor = database.query(
            "Reminder", null, "eventID=?", Array(event.eventID.toString), 
            null, null, "time"
        )
        
        def cursorToDate(cursor: Cursor) = new Date(cursor.getLong(1))

        usingCursor(cursor) {
            cursor.map(cursorToDate).toList
        }
    }

    def addReminder(event: Event, time: Date) = {
        val insertValue = new ContentValues()

        insertValue.put ("eventID", event.eventID: JLong)
        insertValue.put ("time", time.getTime: JLong)

        database.insert ("Reminder", null, insertValue)

        getEvent(event.eventID).get

    }
}

