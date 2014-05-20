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
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog

import android.content.Intent

import android.os.Bundle
import android.view.View

import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.CheckBox

import MaidroidReminder._

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import android.os.Handler
import android.app.ProgressDialog
import android.widget.TextView
import net.fortuna.ical4j.data.CalendarBuilder

class ActivitySync extends Activity with FindView
{
    /*=================================================================
     * Private Fields
     *===============================================================*/
    private lazy val messageBox:   ViewMessageBox = findView (R.id.sync_message_box)
    private lazy val maidImage:    ImageView      = findView (R.id.sync_maid)
    private lazy val usernameEdit: EditText       = findView (R.id.sync_username)
    private lazy val passwordEdit: EditText       = findView (R.id.sync_password)
    private lazy val rememberCheckBox: CheckBox   = findView (R.id.sync_remember)
    private lazy val syncButton:   Button         = findView (R.id.sync_sync)
    private lazy val cancelButton: Button         = findView (R.id.sync_cancel)
    private lazy val preference = new MaidroidPreference (this)

    private lazy val maid = Maid.getMaid (this)
    private val  handler = new Handler {
        override def handleMessage (message: android.os.Message) = message.what match {
            case 0 =>
                val messageView: View = {
                    val msg          = maid.syncError (message.obj.asInstanceOf[String])
                    val layout       = View.inflate (ActivitySync.this, R.layout.dialog, null)
                    val messageView  = layout.findViewById (R.id.dialogMessage).asInstanceOf[TextView]
                    val maidIcon     = layout.findViewById (R.id.dialogMaidIcon).asInstanceOf[ImageView]

                    messageView.setText (msg.message)
                    maidIcon.setImageResource (msg.faceResID)

                    layout
                }

                val dialog = new AlertDialog.Builder (ActivitySync.this)
                dialog.setTitle ("Sync Error!")
                dialog.setView (messageView)
                dialog.setPositiveButton ("OK", null)
                dialog.show ()

            case _ =>
        }
    }

    private def getUserICal: String = {
        val username   = usernameEdit.getText.toString
        val password   = passwordEdit.getText.toString
        val iCalURL    = "https://www.google.com/calendar/dav/%s/events/" format (username)

        val httpClient = new DefaultHttpClient()
        val httpGet    = new HttpGet (iCalURL)

        httpClient.getCredentialsProvider().setCredentials(
            new AuthScope("www.google.com", 443), 
            new UsernamePasswordCredentials(username, password)
        )

        try {
            import scala.io.Source

            val content = httpClient.execute(httpGet).getEntity.getContent
            val text    = Source.fromInputStream(content)(scala.io.Codec.UTF8).mkString

            println (text)
            if (text contains "Unauthorized") {
                throw new Exception ("帳號密碼是不是錯了呢？");
            }

            text
        } catch {
            case e =>  throw e
        } finally {
            httpClient.getConnectionManager().shutdown();        
        }
    }


    /*=================================================================
     * Android Framework
     *===============================================================*/

    /** Called when the activity is first created. */
    override def onCreate(savedInstanceState : Bundle)
    {
        super.onCreate(savedInstanceState)
        setContentView (R.layout.sync)
        setupUI ()
        setupCallbacks ()
    }

    private def setupUI ()
    {
        usernameEdit.setText (preference.getSyncUsername)
        passwordEdit.setText (preference.getSyncPassword)
        rememberCheckBox.setChecked (preference.getSyncRemember)
        
        messageBox.setMaidView (maidImage)
        messageBox.setMessageList (maid.syncMessage)
    }

    private def setupCallbacks ()
    {
        syncButton.setOnClickListener { () =>
            val progressDialog = new ProgressDialog (this)
            val username = usernameEdit.getText.toString
            val password = passwordEdit.getText.toString
            val remember = rememberCheckBox.isChecked ()
            val thread   = new Thread () {
                override def run () {

                    try {
                        import org.maidroid.scalendar.SCalendar
                        val iCal = getUserICal
                        val sCalendar = SCalendar (iCal)
                        println (sCalendar)


                    } catch {
                        case e => 
                            val message = new android.os.Message
                            message.what = 0
                            message.obj  = e.getMessage
                            handler.sendMessage (message)
                    }

                    progressDialog.cancel ()
                }
            }

            preference.setSyncRemember (remember)
            preference.setSyncUsername (username)

            if (remember) {
                preference.setSyncPassword (password)
            }

            (username, password) match {
                case ("",  _) =>
                    maid.showToast (this, maid.noUsername)

                case (_ , "") =>
                    maid.showToast (this, maid.noPassword)

                case (_,   _) =>

                    progressDialog.setIndeterminate (true)
                    progressDialog.setTitle ("Sync")
                    progressDialog.setMessage ("Please wait...")

                    progressDialog.show ()

                    thread.start ()
            }
        }

        cancelButton.setOnClickListener { () =>
            maid.showToast (this, maid.toastNoDisturb)
            finish ()
        }
    }
}

