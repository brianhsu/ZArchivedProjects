/**
 *  Maidroid Reminder
 *  
 *  -------------------------------------------------------------------------
 *  @license
 *
 *  This file is part of Maidroid Reminder
 *
 *  Maidroid Reminder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Maidroid Reminder is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Maidroid Reminder.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *  @author Brian Hsu (brianhsu.hsu [at] gmail.com)
 *  
 */

package org.maidroid.reminder

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog

import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.EditText
import android.widget.ImageView

import android.util.Log

import MaidroidReminder._

class ActivityEdit extends Activity with FindView
{
    /*=================================================================
     * Private Fields
     *===============================================================*/
    private lazy val okButton:    Button     = findView (R.id.add_ok)
    private lazy val cancelButton:Button     = findView (R.id.add_cancel)

    private lazy val dateButton:  Button     = findView (R.id.add_date)
    private lazy val timeButton:  Button     = findView (R.id.add_time)
    private lazy val title:       EditText   = findView (R.id.add_title)
    private lazy val description: EditText   = findView (R.id.add_description)
    private lazy val maidImage:   ImageView  = findView (R.id.add_maid)
    private lazy val messageBox:  ViewMessageBox = findView (R.id.add_message_box)

    private lazy val maid    = Maid.getMaid (this)
    private lazy val action  = getIntent.getAction
    private lazy val dataURI = if (getIntent.getData != null) 
                               Some(getIntent.getData) else None
    private lazy val oldItem = dataURI.flatMap (ReminderItem.get(this,_))

    private lazy val triggerTime = dataURI match {
        case Some(uri) => new TriggerTime (oldItem.get.triggerTime)
        case None      => new TriggerTime (System.currentTimeMillis)
    }

    /*=================================================================
     * Private Helper Method
     *===============================================================*/
    private def setupUI ()
    {
        okButton.setOnClickListener (okClicked)
        cancelButton.setOnClickListener (cancelClicked)
        dateButton.setText (triggerTime.toDateString)
        dateButton.setOnClickListener (dateClicked)
        timeButton.setText (triggerTime.toTimeString)
        timeButton.setOnClickListener (timeClicked)

        messageBox.setMaidView (maidImage)

        if (action == Intent.ACTION_EDIT ) {

            oldItem.foreach ( (item: ReminderItem) => {
                val triggerTime = new TriggerTime (item.triggerTime)

                title.setText (item.title)
                description.setText (item.description)
                dateButton.setText (triggerTime.toDateString)
                timeButton.setText (triggerTime.toTimeString)
            })

            messageBox.setMessageList (maid.editItemMessage)

        } else {
            messageBox.setMessageList (maid.addItemMessage)
        }
    }

    /*=================================================================
     * Callbacks
     *===============================================================*/
    
    private lazy val dateClicked = new View.OnClickListener () 
    {
        def onClick (view: View)
        {
            val dateCallback = new DatePickerDialog.OnDateSetListener () 
            {
                def onDateSet (view: DatePicker, year: Int, 
                               month: Int, date: Int) 
                {
                    triggerTime.setDate (year, month, date)
                    dateButton.setText (triggerTime.toDateString)
                }
            }
    
            val datePicker = new DatePickerDialog (ActivityEdit.this, 
                                                   dateCallback, 
                                                   triggerTime.year, 
                                                   triggerTime.month, 
                                                   triggerTime.date)
            datePicker.show ()
        }
    }

    private lazy val timeClicked = new View.OnClickListener () 
    {
        def onClick (view: View)
        {
            val timeCallback = new TimePickerDialog.OnTimeSetListener () 
            {
                def onTimeSet (view: TimePicker, hour: Int, minute: Int) 
                {
                    triggerTime.setTime (hour, minute)
                    timeButton.setText (triggerTime.toTimeString)
                }
            }
    
            val timePicker = new TimePickerDialog (ActivityEdit.this, 
                                                   timeCallback, 
                                                   triggerTime.hour, 
                                                   triggerTime.minute,
                                                   true)
            timePicker.show ()
        }
    }

    private lazy val okClicked = new View.OnClickListener ()
    {
        def onClick (view: View)
        {
            def isFieldsValid (): Boolean = 
            {
                if (title.getText.length == 0) {
                    maid.showToast (ActivityEdit.this, maid.addItemNoTitle)
                    return false
                }
    
                if (triggerTime.timeInMillis <= System.currentTimeMillis) {
                    maid.showToast (ActivityEdit.this, maid.addItemTimeError)
                    return false
                }
    
                return true
            }
    
            if ( !isFieldsValid ) return
    
            oldItem.foreach ( _.delete() )
    
            val item = ReminderItem.add (ActivityEdit.this, 
                                         title.getText.toString, 
                                         description.getText.toString,
                                         triggerTime.timeInMillis)
    
            item.register ()
            setResult (Activity.RESULT_OK, (new Intent).setData(item.uri))
    
            maid.showToast (ActivityEdit.this, maid.toastOK)
    
            finish ()
        }
    }

    private lazy val cancelClicked = new View.OnClickListener ()
    {
        def onClick (view: View)
        {
            maid.showToast (ActivityEdit.this, maid.toastNoDisturb)
            finish ()
        }
    }

    /*=================================================================
     * Android Framework
     *===============================================================*/

    /** Called when the activity is first created. */
    override def onCreate(savedInstanceState : Bundle)
    {
        super.onCreate(savedInstanceState)

        setContentView (R.layout.add)

        setResult (Activity.RESULT_CANCELED)
        setupUI ()
    }
}

