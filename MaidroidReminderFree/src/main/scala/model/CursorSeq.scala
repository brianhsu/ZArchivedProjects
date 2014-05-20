package org.maidroid.reminder2

import android.database.Cursor

object CursorSeq
{
    implicit def cursorSeqFromCursor(cursor: Cursor) = new CursorSeq(cursor)

    def usingCursor[T](cursor: Cursor)(action: => T) = {
        try {
            action
        } finally {
            cursor.close()
        }
    }
}

class CursorSeq (cursor: Cursor) extends Seq[Cursor]
{
    override val length = cursor.getCount
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

    override def apply (n: Int) : Cursor = {
        cursor.moveToFirst ()
        cursor.moveToPosition (n)
        cursor
    }
}

