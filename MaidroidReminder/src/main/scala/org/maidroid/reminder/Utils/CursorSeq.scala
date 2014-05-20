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

import android.database.Cursor
import android.util.Log

/**
 *  Scala Seq wrapper for android Cursor.
 *
 *  @param  cursor  Cursor from Android.
 */
class CursorSeq (cursor: Cursor) extends Seq[Cursor]
{
    /**
     *  Length of this sequence.
     */
    override val length = cursor.getCount

    /**
     *  Elements of this sequence.
     */
    override val iterator = new Iterator[Cursor] {

        override def hasNext = !cursor.isLast && !cursor.isAfterLast &&
                               !cursor.isClosed

        override def next : Cursor = {

            if (cursor.isBeforeFirst) {
                cursor.moveToFirst ()
            } else {
                cursor.moveToNext ()
            }

            cursor
        }
    }

    /**
     *  Get n-th element in the sequence.
     *
     *  @param  n  The index of element.
     */
    override def apply (n: Int) : Cursor = {
        cursor.moveToFirst ()
        cursor.moveToPosition (n)
        cursor
    }

    override def finalize ()
    {
        super.finalize ()
        cursor.close ()
    }
}

