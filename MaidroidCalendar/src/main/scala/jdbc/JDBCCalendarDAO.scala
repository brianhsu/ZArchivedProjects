package org.maidroid.calendar.model

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.PreparedStatement

import java.util.Date

trait AutoCloseSQL
{
    implicit def autoStatementFromStatement(statement: PreparedStatement) = new AutoCloseStatement(statement)

    class AutoCloseStatement (statement: PreparedStatement) {
        def updateAndClose(): Int = {
            val rowCount = statement.executeUpdate()
            statement.close()
            rowCount
        }

        def query[T] (action: ResultSet => T) = {
            val sqlResult   = statement.executeQuery()
            val scalaResult = action(sqlResult)

            sqlResult.close()
            statement.close()

            scalaResult
        }
    }
}


object XerialSQLite {
    Class.forName("org.sqlite.JDBC");
    def getConnection(url: String) = DriverManager.getConnection(url)
}

object JDBCCalendarDAO
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
}

case class JDBCCalendarDAO(connection: Connection) extends CalendarDAO with AutoCloseSQL
{
    createTable()

    private def eventsFromSQLite (result: ResultSet) = {
        var events: List[Event] = Nil

        while (result.next()) {
            val eventID    = result.getLong(1)
            val calendarID = result.getLong(2)
            val eventURL   = result.getString(3)
            val title      = result.getString(4)
            val startTime  = new Date(result.getLong(5))
            val isFullDay  = result.getInt(6) match {case 1 => true; case _ => false}
            val endTime    = result.getString(7) match {case null => None; 
                                                        case dateTime => Some(new Date(dateTime.toLong))}
            val description = result.getString(8) match {case null => None; case text => Some(text)}
            val recurrence  = result.getString(9) match {case null => None; case text => Some(text)}

            events ::= Event(this, eventID, calendarID, eventURL, title, startTime, 
                             isFullDay, endTime, description, recurrence)
        }

        events.reverse
    }

    private def calendarsFromSQLite (result: ResultSet) = {
        var calendars: List[Calendar] = Nil

        while (result.next()) {
            val calendarID = result.getLong(1)
            val title = result.getString(2)
            val description = result.getString(3) match {
                case null => None
                case text => Some(text)
            }
            val lastUpdated = new Date(result.getLong(4))
            val calendarURL = "mcalendar://jdbc/%d" format(calendarID)

            calendars ::= Calendar(this, calendarID, calendarURL, title, description, lastUpdated)
        }

        calendars.reverse
    }

    private def createTable () {
        val statement = connection.createStatement

        statement.execute(JDBCCalendarDAO.CreateTableSQL.createCalendarTable)
        statement.execute(JDBCCalendarDAO.CreateTableSQL.createEventTable)
        statement.execute(JDBCCalendarDAO.CreateTableSQL.createReminderTable)
        statement.close()
    }

    def createNewCalendar (title: String, description: Option[String] = None) = {

        val sqlInsert = connection.prepareStatement("INSERT INTO Calendar VALUES(null, ?, ?, ?)")

        sqlInsert.setString (1, title)
        sqlInsert.setString (2, description getOrElse null)
        sqlInsert.setLong   (3, (new Date).getTime)
        sqlInsert.updateAndClose()

        val sqlQuery = connection.prepareStatement("SELECT seq FROM sqlite_sequence WHERE name=?")
        sqlQuery.setString (1, "Calendar")
        sqlQuery.query {result =>
            result.next()
            getCalendar(result.getInt(1)).get
        }
    }

    def getCalendar(calendarID: Long): Option[Calendar] = {
        val sqlQuery = connection.prepareStatement("SELECT * FROM Calendar WHERE id=?")
        sqlQuery.setLong (1, calendarID)
        sqlQuery.query {result =>
           val calendars = calendarsFromSQLite(result)

           calendars match {
               case Nil => None
               case calendar :: xs => Some(calendar)
           }
        }
    }

    def getCalendars(): List[Calendar] = {
        val sqlQuery = connection.prepareStatement("SELECT * FROM Calendar ORDER BY id")
        sqlQuery.query{result => calendarsFromSQLite(result)}
    }

    def saveCalendar (newCalendar: Calendar) {
        val sqlUpdate = connection.prepareStatement(
                            "UPDATE Calendar SET title=?, description=?, lastUpdated=? WHERE id=?"
                        )

        sqlUpdate.setString (1, newCalendar.title)
        sqlUpdate.setString (2, newCalendar.description getOrElse null)
        sqlUpdate.setLong   (3, (new Date).getTime)
        sqlUpdate.setLong   (4, newCalendar.calendarID)
        sqlUpdate.updateAndClose()
    }

    def createNewEvent (calendar: Calendar, title: String, startTime: Date, isFullDay: Boolean,
                        endTime: Option[Date] = None,
                        description: Option[String] = None,
                        recurrence: Option[String] = None) = {

        val sqlInsert = connection.prepareStatement("INSERT INTO Event VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?)")

        sqlInsert.setLong   (1, calendar.calendarID)
        sqlInsert.setString (2, "mevent://jdbc/%s/%s" format(calendar.calendarID, (new Date).getTime))
        sqlInsert.setString (3, title)
        sqlInsert.setLong   (4, startTime.getTime)
        sqlInsert.setInt    (5, if (isFullDay) 1 else 0)
        sqlInsert.setString (6, endTime match {case Some(dateTime) => dateTime.getTime.toString; 
                                               case None => null})
        sqlInsert.setString (7, description getOrElse null)
        sqlInsert.setString (8, recurrence getOrElse null)
        sqlInsert.updateAndClose()

        val sqlQuery = connection.prepareStatement("SELECT seq FROM sqlite_sequence WHERE name=?")
        sqlQuery.setString (1, "Event")
        sqlQuery.query {result =>
            result.next()
            getEvent(result.getInt(1)).get
        }
    }

    def getEvents (calendar: Calendar): List[Event] = {
        val sqlQuery = connection.prepareStatement("SELECT * FROM Event WHERE calendarID=? ORDER BY id")

        sqlQuery.setLong (1, calendar.calendarID)
        sqlQuery.query {result => eventsFromSQLite(result)}
    }

    def getEvent(eventID: Long): Option[Event] = {
        val sqlQuery = connection.prepareStatement("SELECT * FROM Event WHERE id=?")
        sqlQuery.setLong (1, eventID)
        sqlQuery.query {result =>
           val events = eventsFromSQLite(result)

           events match {
               case Nil => None
               case event :: xs => Some(event)
           }
        }
    }

    def saveEvent (newEvent: Event) {

        val sqlUpdate = connection.prepareStatement(
                            "UPDATE Event SET calendarID=?, eventURL=?, title=?, startTime=?, " +
                            "isFullDay=?, endTime=?, description=?, recurrence=? WHERE id=?"
                        )

        sqlUpdate.setLong   (1, newEvent.calendarID)
        sqlUpdate.setString (2, newEvent.eventURL)
        sqlUpdate.setString (3, newEvent.title)
        sqlUpdate.setLong   (4, newEvent.startTime.getTime)
        sqlUpdate.setInt    (5, if (newEvent.isFullDay) 1 else 0)
        sqlUpdate.setString (6, newEvent.endTime match {case Some(dateTime) => dateTime.getTime.toString; 
                                                        case None => null})
        sqlUpdate.setString (7, newEvent.description getOrElse null)
        sqlUpdate.setString (8, newEvent.recurrence getOrElse null)
        sqlUpdate.setLong   (9, newEvent.eventID)
        sqlUpdate.updateAndClose()
    }

    def deleteEvent(event: Event): Int = {

        event.getReminders.foreach {reminder => event.deleteReminder(reminder)}

        val sqlUpdate = connection.prepareStatement("DELETE FROM Event WHERE id=?")

        sqlUpdate.setLong(1, event.eventID)
        sqlUpdate.updateAndClose()
    }

    def getReminders(event: Event): List[Date] = {
        val sqlQuery = connection.prepareStatement("SELECT * FROM Reminder WHERE eventID=? ORDER BY time DESC")

        sqlQuery.setLong(1, event.eventID)
        sqlQuery.query { result =>
            var reminders: List[Date] = Nil

            while (result.next()) {
                reminders ::= new Date(result.getLong(2))
            }

            reminders
        }
    }

    def addReminder(event: Event, time: Date) = {
        val sqlAdd = connection.prepareStatement("INSERT INTO Reminder VALUES(?, ?)")

        sqlAdd.setLong (1, event.eventID)
        sqlAdd.setLong (2, time.getTime)

        sqlAdd.updateAndClose()

        getEvent(event.eventID).get
    }

    def deleteReminder(event: Event, time: Date): Int = {
        val sqlDelete = connection.prepareStatement("DELETE FROM Reminder WHERE eventID=? AND time=?")

        sqlDelete.setLong (1, event.eventID)
        sqlDelete.setLong (2, time.getTime)

        sqlDelete.updateAndClose()
    }

    def deleteCalendar (calendar: Calendar) = {

        calendar.getEvents.foreach(event => calendar.deleteEvent(event))

        val sqlDelete = connection.prepareStatement("DELETE FROM Calendar WHERE id=?")
        sqlDelete.setLong  (1, calendar.calendarID)
        sqlDelete.updateAndClose()
    }
}

