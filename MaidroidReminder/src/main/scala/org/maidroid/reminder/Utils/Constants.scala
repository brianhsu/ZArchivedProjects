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

import android.content.UriMatcher
import android.net.Uri

object MaidroidReminder
{
    val PACKAGE = "org.maidroid.reminder"

    object ACTION {

        // Action for list ReminderItem
        val LIST_WEEK    = PACKAGE + "." + "LIST_WEEK"
        val LIST_MONTH   = PACKAGE + "." + "LIST_MONTH"
        val LIST_ALL     = PACKAGE + "." + "LIST_ALL"
        val LIST_OUTDATE = PACKAGE + "." + "LIST_OUTDATE"

        // Activity for start Preference Activity
        val PREFERENCE      = PACKAGE + "." + "PREFERENCE"

        // 尚未 Refactor
        val CONFIRM_REMIND  = PACKAGE + "." + "CONFIRM_REMIND"
        val RECEIVE_REMIND  = PACKAGE + "." + "RECEIVE_REMIND"
        val ADD_REMIND_ITEM = PACKAGE + "." + "ADD_REMIND_ITEM"
        val SYNC            = PACKAGE + "." + "SYNC"

    }

}


/**
 *  Constants of Reminder
 */
object Reminder
{
    // Software Pacakges
    val Authority = "org.maidroid.reminder"

    // Content Type and URI definition
    val ContentUri = Uri.parse ("content://" + Authority + "/reminder")
    val ContentType = "vnd.android.cursor.dir/vnd.maidroid.reminder"
    val ContentItemType = "vnd.android.cursor.item/vnd.maidroid.reminder"

    // Database Field
    val DBTableName = "Reminder"
    val ID          = "_id"
    val Title       = "title"
    val Description = "description"
    val TriggerTime = "triggerTime"

    // Used by UriMatcher
    val NoMatch        = UriMatcher.NO_MATCH
    val IsReminder     = 1  // URI format is content://.../reminder
    val IsReminderItem = 2  // URI format is content://.../reminder/id
}

