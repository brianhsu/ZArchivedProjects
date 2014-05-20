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

import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.TextView

import java.util.{Calendar => JCalendar, Date}
import java.text.SimpleDateFormat

class AddActivity extends TypedActivity with MaidDialogUI with MaidToast {

    lazy val maid = Maid.getMaid(this, 'Maro)
    lazy val calendarDAO = Calendar.getCalendarDAO(this)
    lazy val calendar = calendarDAO.getCalendars(0)

    // Main UI
    lazy val titleText = findView(TR.add_title)
    lazy val dateButton = findView(TR.add_date)
    lazy val timeButton = findView(TR.add_time)
    lazy val repeatSpinner = findView(TR.add_repeat)
    lazy val descriptionText = findView(TR.add_description)
    lazy val okButton = findView(TR.add_ok)
    lazy val cancelButton = findView(TR.add_cancel)

    // For Maid Dialog
    lazy val context = this
    lazy val messageBox = findView(TR.add_message)
    lazy val maidImage = findView(TR.add_maid)
    lazy val scrollView = findView(TR.add_scrollView)
    lazy val maidDialog = new MaidDialog(this)
    lazy val eventTime = JCalendar.getInstance

    private var timeIsSet = false
    private var dateIsSet = false

    private def initRepeatSpinner ()
    {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.repeat_settings, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeatSpinner.setAdapter(adapter)
    }

    private def initListener()
    {
        val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
        val timeFormatter = new SimpleDateFormat("HH:mm")

        val dateHandler = new OnDateSetListener() {
            override def onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
                eventTime.set(JCalendar.YEAR, year)
                eventTime.set(JCalendar.MONTH, month)
                eventTime.set(JCalendar.DAY_OF_MONTH, day)
                dateButton.setText(dateFormatter.format(eventTime.getTime))
                dateIsSet = true
            }
        }

        val timeHandler = new OnTimeSetListener() {
            override def onTimeSet(view: TimePicker, hour: Int, minute: Int) {
                eventTime.set(JCalendar.HOUR_OF_DAY, hour)
                eventTime.set(JCalendar.MINUTE, minute)
                eventTime.set(JCalendar.SECOND, 0)
                timeButton.setText(timeFormatter.format(eventTime.getTime))
                timeIsSet = true
            }
        }

        titleText.setOnFocusChangeListener { hasFocus: Boolean => 
            //if (hasFocus) maidDialog.setMessages(Message("請一定要填標題喲") :: Nil)
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
            AddActivity.this.finish()
        }

        okButton.setOnClickListener { view: View =>
            val currentTime = (new Date).getTime
            val eventStartTime = eventTime.getTime.getTime

            val hasTitle = (() => titleText.getText.length > 0, maid.needTitle)
            val hasCorrectTime = (
                () => eventStartTime > currentTime && dateIsSet && timeIsSet, 
                maid.timeWrong
            )

            assertWith(hasTitle, hasCorrectTime) {
                val event = calendarDAO.createNewEvent(
                    calendar = calendar, 
                    title = titleText.getText.toString, 
                    startTime = eventTime.getTime,
                    isFullDay = false,
                    recurrence = RFC2445Repeat(repeatSpinner.getSelectedItemPosition),
                    description = descriptionText.getText.toString match {
                        case "" => None
                        case desc => Some(desc)
                    }
                ).addReminder(eventTime.getTime)

                event.registerAlarm(AddActivity.this)
                showToast(maid.addEventOK)
                AddActivity.this.finish()
            }
        }
    }

    override def onCreate(savedInstanceState: Bundle) 
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add)
        maidDialog.setMessages(maid.addEventMessage)
        initRepeatSpinner()
        initListener()
    }
}
