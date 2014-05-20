package org.maidroid.reminder2

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

import org.maidroid.calendar.model.test._

import android.content.Context

class AndroidCalendarTest(dbHelper: CalendarDBHelper) extends CalendarTestBase
{
    def getCalendarDAO = new AndroidDBCalendar(dbHelper)
}

class TestActivity extends Activity {

    def runTest () {
        val dbHelper  = new CalendarDBHelper(this, "test.db", 1)
        val testSuite = new AndroidCalendarTest(dbHelper)
        testSuite.execute()
        dbHelper.close()
    }

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        setContentView(new TextView(this) {
            setText("ScalaTest Activity, please check adb logcat V output")
        })

        runTest()
    }
}
