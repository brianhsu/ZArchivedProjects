/**
 *  Maidroid Reminder Content Provider
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *  @author Brian Hsu (brianhsu.hsu [at] gmail.com)
 *
 */

package org.maidroid.reminder

import android.content.ContentProvider
import android.content.Context
import android.content.UriMatcher
import android.content.ContentUris

import android.net.Uri
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.database.SQLException

import android.content.ContentValues
import android.text.TextUtils


private object ReminderDB
{
    val Name  = "MaidroidReminder.db"
    val Version   = 1
    val createSQL = """
        CREATE TABLE Reminder (
            _id         INTEGER PRIMARY KEY,
            title       TEXT    NOT NULL,
            description TEXT,
            triggerTime LONG
        )
    """
}

private object ProviderRemindItem
{
    private val uriMatcher = new UriMatcher (Reminder.NoMatch)

    uriMatcher.addURI (Reminder.Authority, "reminder", Reminder.IsReminder)
    uriMatcher.addURI (Reminder.Authority, "reminder/#", 
                       Reminder.IsReminderItem)
}

class ProviderRemindItem extends ContentProvider
{
    
    class DBHelper (context: Context) extends 
          SQLiteOpenHelper (context, ReminderDB.Name, null, 
                            ReminderDB.Version)
    {
        override def onCreate (database: SQLiteDatabase)
        {
            database.execSQL (ReminderDB.createSQL)
        }

        override def onUpgrade (database: SQLiteDatabase, oldVersion: Int,
                                newVersion: Int)
        {
            database.execSQL ("DROP TABLE IF EXISTS " + 
                              Reminder.DBTableName )

            database.execSQL (ReminderDB.createSQL)
        }
    }
 
    private lazy val dbHelper  = new DBHelper (getContext())

    override def delete (uri: Uri, selection: String, 
                         selectionArgs: Array[String]) : Int =
    {
        val db = dbHelper.getWritableDatabase

        ProviderRemindItem.uriMatcher.`match` (uri) match {
            case Reminder.IsReminder => 
                db.delete (Reminder.DBTableName, selection, selectionArgs)

            case Reminder.IsReminderItem => 
                val reminderID = uri.getPathSegments.get(1)
                val hasSelection = !TextUtils.isEmpty (selection)
                val newSelection = if (hasSelection) 
                                        " AND ( " + selection + " ) " 
                                   else
                                        ""
                val count = db.delete (Reminder.DBTableName, 
                                       Reminder.ID + " = " + reminderID + 
                                       newSelection,
                                       selectionArgs)

                getContext.getContentResolver.notifyChange (uri, null)

                count

            case _ =>
                throw new IllegalArgumentException ("Unknow URI:" + uri)
        }

    }

    override def update (uri: Uri, values: ContentValues, selection: String, 
                         selectionArgs: Array[String]) : Int =
    {
        val db = dbHelper.getWritableDatabase

        ProviderRemindItem.uriMatcher.`match` (uri) match {
            case Reminder.IsReminder => 
                db.update (Reminder.DBTableName, values, selection, 
                           selectionArgs)

            case Reminder.IsReminderItem => 
                val reminderID = uri.getPathSegments.get(1)
                val hasSelection = !TextUtils.isEmpty (selection)
                val newSelection = if (hasSelection) 
                                        " AND ( " + selection + " ) " 
                                   else
                                        ""
                val count = db.update (Reminder.DBTableName, values,
                                       Reminder.ID + " = " + reminderID + 
                                       newSelection,
                                       selectionArgs)

                getContext.getContentResolver.notifyChange (uri, null)

                count

            case _ =>
                throw new IllegalArgumentException ("Unknow URI:" + uri)
        }

        return 0
    }

    override def insert (uri: Uri, initValues: ContentValues) : Uri = 
    {
        val values = if (initValues != null) initValues else (new ContentValues)

        def insertToDB () : Uri = {
            val db = dbHelper.getWritableDatabase
            val rowID = db.insert (Reminder.DBTableName, "", values)

            if (rowID > 0) {
                val reminderURI = 
                    ContentUris.withAppendedId (Reminder.ContentUri, rowID)

                getContext.getContentResolver.notifyChange (reminderURI, null)
                return reminderURI
            }

            throw new SQLException("Failed to insert row:" + uri)
        }

        def insertWithRowID (): Uri = {
            val rowID       = initValues.getAsLong   ("rowID").asInstanceOf[Long]
            val title       = initValues.getAsString (Reminder.Title)
            val description = initValues.getAsString (Reminder.Description)
            val triggerTime = initValues.getAsLong   (Reminder.TriggerTime)

            val db  = dbHelper.getWritableDatabase
            val sql = """insert or replace into %s values (%d, "%s", "%s", %d)""" format (Reminder.DBTableName, rowID,
                                                                                         title, description,
                                                                                         triggerTime)
            db.execSQL (sql)

            ContentUris.withAppendedId (Reminder.ContentUri, rowID)
        }

        ProviderRemindItem.uriMatcher.`match` (uri) match {
            case Reminder.IsReminder if initValues.containsKey("rowID") =>
                insertWithRowID ()

            case Reminder.IsReminder =>
                insertToDB ()

            case _ =>
                throw new IllegalArgumentException ("Unknow URI:" + uri)
        }
    }

    override def query (uri: Uri, columns: Array[String], 
                        selection: String, selectionArgs: Array[String], 
                        sortOrder: String) : Cursor =
    {
        val db = dbHelper.getReadableDatabase

        ProviderRemindItem.uriMatcher.`match` (uri) match {
            case Reminder.IsReminder => 
                db.query (Reminder.DBTableName, columns, selection, 
                          selectionArgs, null, null, sortOrder)

            case Reminder.IsReminderItem => 
                val reminderID = uri.getPathSegments.get(1)
                val hasSelection = !TextUtils.isEmpty (selection)
                val newSelection = if (hasSelection) 
                                        " AND ( " + selection + " ) " 
                                   else
                                        ""
                db.query (Reminder.DBTableName, columns, 
                          Reminder.ID + " = " + reminderID + newSelection, 
                          selectionArgs, null, null, sortOrder)

            case _ =>
                throw new IllegalArgumentException ("Unknow URI:" + uri)
       } 
    }

    override def getType (uri: Uri) : String =
    {
        ProviderRemindItem.uriMatcher.`match` (uri) match {
            case Reminder.IsReminder => 
                Reminder.ContentType

            case Reminder.IsReminderItem =>
                Reminder.ContentItemType

            case _ =>
                throw new IllegalArgumentException ("Unknow URI:" + uri)
        }

    }

    override def onCreate () : Boolean =
    {
        return true
    }

}
