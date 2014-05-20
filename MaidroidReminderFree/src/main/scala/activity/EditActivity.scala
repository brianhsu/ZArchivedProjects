package org.maidroid.reminder2

import org.maidroid.reminder2.AndroidDBEvent._
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener

import android.app.TimePickerDialog
import android.app.TimePickerDialog._

import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.ArrayAdapter

import android.widget.TextView

import java.util.{Calendar => JCalendar, Date}
import java.text.SimpleDateFormat

class EditActivity extends TypedActivity with MaidDialogUI with MaidToast {

    lazy val maid = Maid.getMaid(this, 'Maro)
    lazy val calendarDAO = Calendar.getCalendarDAO(this)
    lazy val calendar = calendarDAO.getCalendars(0)

    // Main UI
    lazy val titleText = findView(TR.edit_title)
    lazy val dateButton = findView(TR.edit_date)
    lazy val timeButton = findView(TR.edit_time)
    lazy val descriptionText = findView(TR.edit_description)
    lazy val okButton = findView(TR.edit_ok)
    lazy val cancelButton = findView(TR.edit_cancel)
    lazy val repeatSpinner = findView(TR.edit_repeat)

    // For Maid Dialog
    lazy val context = this
    lazy val messageBox = findView(TR.edit_message)
    lazy val maidImage = findView(TR.edit_maid)
    lazy val scrollView = findView(TR.edit_scrollView)
    lazy val maidDialog = new MaidDialog(this)
    lazy val event = AndroidDBEvent(this, getIntent).event
    lazy val eventTime = JCalendar.getInstance

    private def updateDateTimeButton()
    {
        val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
        val timeFormatter = new SimpleDateFormat("HH:mm")

        dateButton.setText(dateFormatter.format(eventTime.getTime))
        timeButton.setText(timeFormatter.format(eventTime.getTime))
    }

    private def loadEventDataToUI()
    {
        titleText.setText(event.title)
        event.description.foreach { descriptionText.setText }
        eventTime.setTime(event.startTime)
        updateDateTimeButton()
    }

    private def initRepeatSpinner ()
    {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.repeat_settings, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeatSpinner.setAdapter(adapter)

        event.recurrence.foreach { repeatNote => 
            repeatSpinner.setSelection(RFC2445Repeat(repeatNote))
        }
    }

    private def initListener()
    {
        val dateHandler = new OnDateSetListener() {
            override def onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
                eventTime.set(JCalendar.YEAR, year)
                eventTime.set(JCalendar.MONTH, month)
                eventTime.set(JCalendar.DAY_OF_MONTH, day)
                updateDateTimeButton()
            }
        }

        val timeHandler = new OnTimeSetListener() {
            override def onTimeSet(view: TimePicker, hour: Int, minute: Int) {
                eventTime.set(JCalendar.HOUR_OF_DAY, hour)
                eventTime.set(JCalendar.MINUTE, minute)
                eventTime.set(JCalendar.SECOND, 0)
                updateDateTimeButton()
            }
        }

        dateButton.setOnClickListener { view: View =>
            val datePicker = new DatePickerDialog(
                this, dateHandler,
                eventTime.get(JCalendar.YEAR), 
                eventTime.get(JCalendar.MONTH),
                eventTime.get(JCalendar.DAY_OF_MONTH)
            )

            datePicker.show()
        }

        timeButton.setOnClickListener { view: View =>
            val timePicker = new TimePickerDialog(
                this, timeHandler,
                eventTime.get(JCalendar.HOUR_OF_DAY),
                eventTime.get(JCalendar.MINUTE),
                true
            )

            timePicker.show()
        }

        cancelButton.setOnClickListener { view: View => 
            this.finish()
        }

        okButton.setOnClickListener { view: View =>
            val currentTime = (new Date).getTime
            val eventStartTime = eventTime.getTime.getTime

            val hasTitle = (() => titleText.getText.length > 0, maid.needTitle)
            val hasCorrectTime = (() => eventStartTime > currentTime, maid.timeWrong)

            assertWith(hasTitle, hasCorrectTime) {
                
                clearReminders()

                val newEvent = event.copy (
                    title = titleText.getText.toString, 
                    startTime = eventTime.getTime,
                    recurrence = RFC2445Repeat(repeatSpinner.getSelectedItemPosition),
                    description = descriptionText.getText.toString match {
                        case "" => None
                        case desc => Some(desc)
                    }
                )
                
                newEvent.addReminder(eventTime.getTime)
                calendarDAO.saveEvent(newEvent)
                newEvent.registerAlarm(this)

                this.finish()
            }
        }
    }

    private def clearReminders () 
    {
        val oldReminders = event.getReminders
        event.unregisterAlarm(this)
        oldReminders.foreach(event.deleteReminder)
    }

    override def onCreate(savedInstanceState: Bundle) 
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit)
        loadEventDataToUI()
        maidDialog.setMessages(maid.editEventMessage)
        initRepeatSpinner()
        initListener()
    }
}
