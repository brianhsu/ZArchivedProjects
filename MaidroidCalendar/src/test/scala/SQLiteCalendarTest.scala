package org.maidroid.calendar.model.test

import org.scalatest.FeatureSpec
import org.scalatest.matchers.ShouldMatchers

import org.maidroid.calendar.model.JDBCCalendarDAO
import org.maidroid.calendar.model.XerialSQLite

class SQLiteCalendarTest extends CalendarTestBase {
    def getCalendarDAO = JDBCCalendarDAO(XerialSQLite.getConnection("jdbc:sqlite::memory:"))
}
