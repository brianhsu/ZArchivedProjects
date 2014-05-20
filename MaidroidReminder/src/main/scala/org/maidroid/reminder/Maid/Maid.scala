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

import android.app.Notification
import android.app.PendingIntent

import android.content.Context
import android.content.Intent

import android.media.RingtoneManager

import android.net.Uri

import android.view.Gravity
import android.view.ViewGroup.{LayoutParams => VGParams}

import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams

import android.util.Log
import scala.collection.immutable.StringOps

/**
 *  This trait defined what a Maid should have.
 *
 */
trait Maid
{
    type Action = Symbol

    val context: Context

    /*=================================================================
     * Basic Information about a Maid
     *===============================================================*/
    def name: String
    def notificationIcon: Int
    def room: Int

    def notificationSound: String

    def itemListMessage (action: String): List[Message]
    def remindMessage (item: ReminderItem) : List[Message]
    def addItemMessage () : List[Message]
    def editItemMessage () : List[Message]
    def aboutMessage (): List[Message]
    def itemSelectedMessage (item: ReminderItem): List[Message]
    def syncMessage: List[Message]
    def syncError (reason: String): Message

    def delTitle (item: ReminderItem): String
    def delMessage (item: ReminderItem): Message

    def toastOK: Message
    def toastNoDisturb   :Message
    def addItemNoTitle   :Message
    def addItemTimeError :Message
    def noPassword :Message
    def noUsername :Message

    def excuseMe: Message
    
    def notificationTitle   (item: ReminderItem): String
    def notificationContent (item: ReminderItem): String


    /*=================================================================
     * Private Fields
     *===============================================================*/


    private def intentWhenClicked (context: Context, item: ReminderItem) = 
    {
        val requestCode = 0 // Not used.
        val flags       = 0 // Default setting.

        // When user clicks message, bring user to our application.
        val intent  = new Intent (MaidroidReminder.ACTION.CONFIRM_REMIND, 
                                  item.uri)

        PendingIntent.getActivity (context, requestCode, intent, flags)
    }

    /*=================================================================
     * Public Methods
     *===============================================================*/

    /**
     *  Create a android Notification object.
     */
    def createNotification (context: Context, 
                            item: ReminderItem) : Notification = 
    {
        val tickerText = this.excuseMe.message
        val title      = this.notificationTitle (item)
        val content    = this.notificationContent (item)
        val when       = System.currentTimeMillis();
        val preference = new MaidroidPreference (context)
        val notification = new Notification (notificationIcon, tickerText,
                                             when)
        def setNotificationSound ()
        {
            // Get default Notification Sound from users's setting
            val defaultSound = RingtoneManager.getActualDefaultRingtoneUri (
                                   context, RingtoneManager.TYPE_NOTIFICATION
                               )
    
            // Set notification sound in the following order
            //
            //   * Sound specified by Maid
            //   * Default notification sound selected by user
            //   * No Sound
            //
            if (notificationSound != "" && !preference.systemNotificationSound) {
                notification.sound = Uri.parse(notificationSound)
            } else if (defaultSound != null) {
                notification.sound = defaultSound
            }
        }

        def setNotificationFlags ()
        {
            notification.flags    |= Notification.FLAG_AUTO_CANCEL
            notification.defaults |= Notification.DEFAULT_LIGHTS | 
                                     Notification.DEFAULT_VIBRATE

            if (preference.loopNotification) {
                notification.flags |= Notification.FLAG_INSISTENT
            }
        }

        setNotificationFlags ()
        setNotificationSound ()

        notification.setLatestEventInfo (context, title, content, 
                                         intentWhenClicked(context, item))
        notification
    }

    def showToast (context: Context, message: Message)
    {
        val toast     = new Toast (context)
        val textView  = new TextView (context)
        val imageView = new ImageView (context)
        val layout    = new LinearLayout (context)

        textView.setText (message.message)
        textView.setTextSize (20)
        textView.setGravity (Gravity.CENTER)

        imageView.setImageResource (message.faceResID)

        layout.setOrientation (LinearLayout.VERTICAL)
        layout.setGravity (Gravity.FILL)
        layout.setBackgroundResource (android.R.drawable.toast_frame)

        val imageParams = new LayoutParams (VGParams.FILL_PARENT,
                                            VGParams.FILL_PARENT, 1)

        val textParams  = new LayoutParams (VGParams.FILL_PARENT,
                                            VGParams.FILL_PARENT, 5)

        layout.addView (imageView, imageParams)
        layout.addView (textView,  textParams)

        toast.setGravity (Gravity.FILL, 0, 0)
        toast.setView (layout)
        toast.setDuration (new MaidroidPreference(context).toastLength)

        toast.show ()
        message.play (context)
    }

    implicit def resID2String (resID: Int)     = context.getString (resID).stripMargin
    implicit def resID2StringOps (resID: Int)  = new StringOps (resID2String(resID))
}

object Maid
{

    private var maids: Map[String, Maid] = null

    def maidMap (mContext: Context) = 
    {
        if (maids == null) {
            maids = Map ("Mia"    -> new Mia    {val context = mContext},
                         "Moetan" -> new Moetan {val context = mContext})
        }

        maids
    }

    def getMaid (mContext: Context) = {
        val maidroidPreference = new MaidroidPreference (mContext)

        maidMap(mContext).getOrElse (maidroidPreference.maid, new Mia {val context = mContext})
    }
}
