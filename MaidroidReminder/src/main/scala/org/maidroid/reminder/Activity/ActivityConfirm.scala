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

import android.content.Context
import android.os.Bundle
import android.view.View

import android.widget.Button
import android.widget.ImageView
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast

import android.util.Log


/**
 *  Activity let user confirm a certain ReminderItem
 */
class ActivityConfirm extends Activity with FindView
{
    /*=================================================================
     * Private Fields
     *===============================================================*/
    private lazy val uri          = getIntent.getData
    private lazy val item         = ReminderItem.get (this, uri).get
    private lazy val triggerTime  = new TriggerTime (System.currentTimeMillis)
    private lazy val maid         = Maid.getMaid (this)

    private lazy val messageBox: ViewMessageBox = findView (R.id.confirm_message)
    private lazy val maidImage:  ImageView  = findView (R.id.confirm_maid_image)
    private lazy val okButton:   Button     = findView (R.id.confirm_ok)
    private lazy val laterButton:Button     = findView (R.id.confirm_later)
    
    /*=================================================================
     * Helper Method / Object
     *===============================================================*/
   
    /**
     *  Show Date/Time Picker Dialog when user click "Remind Me Later"
     *
     *  Control Flow:
     *
     *    1. DatePicker Dialog
     *
     *    2. When user click "SET" button on DatePicker dialog, 
     *       show TimePicker Dialog
     *
     *    3. When user click "SET" button on TimePicker dialog,
     *       invoke pass-in injection function.
     *
     *  @pram  injectWhenSet  Injection function called when date/time set.
     *
     */
    private def showLaterDialog (injectWhenSet: () => Unit)
    {
        val timeCallback = new TimePickerDialog.OnTimeSetListener () 
        {
            def onTimeSet (view: TimePicker, hour: Int, minute: Int) 
            {
                triggerTime.setTime (hour, minute)
                injectWhenSet ()
            }
        }

        val dateCallback = new DatePickerDialog.OnDateSetListener () 
        {
            def onDateSet (view: DatePicker, year: Int, month: Int, date: Int) 
            {
                val timePicker = new TimePickerDialog (ActivityConfirm.this, 
                                                       timeCallback,
                                                       triggerTime.hour, 
                                                       triggerTime.minute, 
                                                       true)

                triggerTime.setDate (year, month, date)
                timePicker.show ()
            }
        }

        val datePicker = new DatePickerDialog (this, dateCallback, 
                                               triggerTime.year, 
                                               triggerTime.month, 
                                               triggerTime.date)
        datePicker.show ()
    }


    private def initActivity ()
    {
        messageBox.setMaidView (maidImage)
        maidImage.setBackgroundResource (maid.room)

        okButton.setOnClickListener (okClicked)
        laterButton.setOnClickListener (laterClicked)
    }

    /*=================================================================
     * Callback Methods
     *===============================================================*/

    def laterConfirmed ()
    {
        ReminderItem.add (this, item.title, item.description,
                          triggerTime.timeInMillis).register()
        item.delete ()

        maid.showToast (this, maid.toastOK)
        finish ()
    }

    lazy val laterClicked = new View.OnClickListener ()
    {
        def onClick (view: View)
        {
            showLaterDialog (laterConfirmed)
        }
    }

    lazy val okClicked = new View.OnClickListener () 
    {
        def onClick (view: View) 
        {
            maid.showToast (ActivityConfirm.this, maid.toastNoDisturb)
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
        
        setContentView (R.layout.confirm)

        initActivity ()

        messageBox.setMessageList (maid.remindMessage (item))
        messageBox.injectWhenLast = () => {
            okButton.setEnabled   (true)
            laterButton.setEnabled (true)
        }

    }
}
