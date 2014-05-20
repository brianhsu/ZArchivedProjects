package org.maidroid.reminder2

import org.maidroid.reminder2.AndroidDBEvent._

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener

import android.app.TimePickerDialog
import android.app.TimePickerDialog._

import android.os.Bundle
import android.content.Context
import android.content.Intent

import android.widget.TextView
import android.widget.ImageView
import android.widget.DatePicker
import android.widget.TimePicker
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable

import java.util.{Calendar => JCalendar, Date}

class ViewActivity extends Activity with TypedActivity with MaidDialogUI with MaidToast
{
    private lazy val fromIntent = this.getIntent
    private lazy val event = AndroidDBEvent(this, fromIntent).event

    lazy val maid = Maid.getMaid(this, 'Maro)

    // For Maid Dialog UI
    lazy val context = this
    lazy val messageBox = findView(TR.view_message)
    lazy val maidImage = findView(TR.view_maid)
    lazy val scrollView = findView(TR.view_scrollView)
    lazy val maidDialog = new MaidDialog(this)

    // Main UI
    private lazy val okButton = findView(TR.view_ok)
    private lazy val remindLaterButton = findView(TR.view_remind_later)
    private lazy val eventTime = JCalendar.getInstance

    private def showDateDialog()
    {
        val dateHandler = new OnDateSetListener() {
            override def onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
                eventTime.set(JCalendar.YEAR, year)
                eventTime.set(JCalendar.MONTH, month)
                eventTime.set(JCalendar.DAY_OF_MONTH, day)
                showTimeDialog()
            }
        }

        val datePicker = new DatePickerDialog(
            this, dateHandler,
            eventTime.get(JCalendar.YEAR),
            eventTime.get(JCalendar.MONTH),
            eventTime.get(JCalendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

    private def showTimeDialog()
    {
        val currentTime = (new Date).getTime
        val hasCorrectTime = (() => eventTime.getTime.getTime > currentTime, maid.timeWrong)

        val timeHandler = new OnTimeSetListener() {
            override def onTimeSet(view: TimePicker, hour: Int, minute: Int) {
                eventTime.set(JCalendar.HOUR_OF_DAY, hour)
                eventTime.set(JCalendar.MINUTE, minute)
                eventTime.set(JCalendar.SECOND, 0)

                assertWith(hasCorrectTime) {
                    event.addReminder(eventTime.getTime)
                    println(event.getReminders)
                    event.registerAlarm(ViewActivity.this)
                    showToast(maid.addEventOK)
                    ViewActivity.this.finish()
                }
            }
        }

        val timePicker = new TimePickerDialog(
            this, timeHandler,
            eventTime.get(JCalendar.HOUR_OF_DAY),
            eventTime.get(JCalendar.MINUTE),
            true
        )

        timePicker.show()
    }

    private def initListener()
    {
        okButton.setOnClickListener { view: View => this.finish() }
        remindLaterButton.setOnClickListener { view: View => showDateDialog() }
    }

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view)
        maidDialog.setMessages(maid.reminderMessage(event))
        initListener()
    }

}
