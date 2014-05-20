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

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Activity

import android.content.ContentValues
import android.content.ContentUris
import android.content.Context
import android.content.Intent

import android.database.Cursor
import android.os.Bundle

import android.net.Uri
import android.util.Log

import android.widget.ListAdapter
import android.widget.ArrayAdapter

/**
 *  ReminderItem present a reminder item in memory.
 *
 *  @param  context       Context from android.
 *  @param  id            ID of this item.
 *  @param  title         Title of this item.
 *  @param  description   Description of this item.
 *  @param  triggerTime   When to notifiy user.
 */
case class ReminderItem private (context: Context, 
                                 id: Int, title: String, 
                                 description: String, 
                                 triggerTime: Long)
{
    /*=================================================================
     * Private Fields
     *===============================================================*/

    private lazy val alarmManager = context.
                                    getSystemService(Context.ALARM_SERVICE).
                                    asInstanceOf[AlarmManager]

    private lazy val broadcastIntent = 
    {
        val requestCode = 0 // Not Used
        val flags       = 0 // Flags
        val intent = new Intent (MaidroidReminder.ACTION.RECEIVE_REMIND, uri)

        PendingIntent.getBroadcast (context, requestCode, intent, flags)
    }

    
    /*=================================================================
     * Public Fields
     *===============================================================*/

    /**
     *  Android URI of this item.
     */
    lazy val uri : Uri = Uri.parse (Reminder.ContentUri + "/" + id)

    /**
     *  Delete this item.
     */
    def delete ()
    {
        unregister ()
        ReminderItem.delete (context, this)
    }

    /**
     *  Register Notifcation to notifiy user when triggerTime arrived.
     */
    def unregister () = alarmManager.cancel (broadcastIntent)

    /**
     *  Unregister notification
     */
    def register   () = alarmManager.set (AlarmManager.RTC_WAKEUP, 
                                          triggerTime, broadcastIntent)

    override def toString () = title
}

/**
 *  Companion Object of ReminderItem class.
 */
object ReminderItem
{
    /*=================================================================
     * Private Fields
     *===============================================================*/

    /**
     *  Delete ReminderItem from database.
     *
     *  @param  context   Android context.
     *  @param  item      ReminderItem to be deleted.
     */
    private def delete (context: Context, item: ReminderItem)
    {
        context.getContentResolver.delete (item.uri, null, null)
    }

    /**
     *  Get a ReminderItem from database.
     *
     *  @param  context  Android context.
     *  @param  cursor   Cursor pointed to the item in database.
     */
    private def get (context: Context, 
                     cursor: Cursor) : Option[ReminderItem] = 
    {
        if (cursor == null) {
            return None
        }

        val colID          = cursor.getColumnIndex (Reminder.ID)
        val colTitle       = cursor.getColumnIndex (Reminder.Title)
        val colDescription = cursor.getColumnIndex (Reminder.Description)
        val colTriggerTime = cursor.getColumnIndex (Reminder.TriggerTime)

        val id          = cursor.getInt    (colID)
        val title       = cursor.getString (colTitle)
        val description = cursor.getString (colDescription)
        val triggerTime = cursor.getLong   (colTriggerTime)

        Some (ReminderItem (context, id, title, description, 
                            triggerTime))
    }


    /*=================================================================
     * Public Fields
     *===============================================================*/

    /**
     *  Get all ReminderItem from database.
     *
     *  @param  context  Android context.
     */
    def getAll (context: Context) : Seq[ReminderItem] = 
    {
        val cursorSeq = new CursorSeq (context.getContentResolver.
                                       query (Reminder.ContentUri, 
                                              null, null, null, 
                                              Reminder.TriggerTime))

        val items = for (cursor <- cursorSeq; item = get(context, cursor) if 
                         item != None) yield item.get

        items
    }

    /**
     *  Get specific ReminderItem from android content:// URI
     *
     *  @param   context  Android context.
     *  @param   uri      content:// URI of the item
     */
    def get (context: Context, uri: Uri) : Option[ReminderItem] = 
    {
        try {
            val allItem = getAll (context)
            Some (allItem.filter (_.uri.toString == uri.toString).head)
        } catch {
            case _ => None
        }
    }


    /**
     *  Add ReminderItem to database.
     *
     *  @param  context  Android context.
     *  @param  title         Title of this item.
     *  @param  description   Description of this item.
     *  @param  triggerTime   When to notifiy user.
     */
    def add (context: Context, title: String, description: String, 
             triggerTime : Long) : ReminderItem = 
    {
        val values = new ContentValues

        values.put (Reminder.Title,       title)
        values.put (Reminder.Description, description)
        values.put (Reminder.TriggerTime, triggerTime.asInstanceOf[java.lang.Long])

        val uri = context.getContentResolver.insert (Reminder.ContentUri,
                                                     values)
        val id  = uri.getPathSegments.get(1).toInt

        ReminderItem (context, id, title, description, triggerTime)
    }

    def add (context: Context, rowID: Long, title: String, description: String, 
             triggerTime : Long) : ReminderItem = 
    {
        val values = new ContentValues

        values.put ("rowID",              rowID.asInstanceOf[java.lang.Long])
        values.put (Reminder.Title,       title)
        values.put (Reminder.Description, description)
        values.put (Reminder.TriggerTime, triggerTime.asInstanceOf[java.lang.Long])

        val uri = context.getContentResolver.insert (Reminder.ContentUri,
                                                     values)
        val id  = uri.getPathSegments.get(1).toInt

        ReminderItem (context, id, title, description, triggerTime)
    }

    def getListAdapter (context: Context, 
                        condition: ReminderItem => Boolean) : ListAdapter =
    {
        val items = getAll (context).filter (condition).toArray

        new ArrayAdapter (context, android.R.layout.simple_list_item_1,
                          items)
    }

    def getListAdapter (context: Context) : ListAdapter = 
    {
        getListAdapter (context, _ => true)
    }
}
