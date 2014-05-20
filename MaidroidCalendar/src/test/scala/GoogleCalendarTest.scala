package org.maidroid.calendar.model.test

import org.scalatest.FeatureSpec
import org.scalatest.matchers.ShouldMatchers

import org.maidroid.calendar.model.GoogleCalendarDAO

class GoogleCalendarTest extends CalendarTestBase {

    lazy val username = {
        print ("Enter Google Calendar username:")
        Console.readLine()
    }

    lazy val password = {
        print ("Enter password:")
        Console.readLine()
    }

    def getCalendarDAO = {
        val service = GoogleCalendarDAO(username, password)
        service.login ()
        service
    }

}
