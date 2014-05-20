package org.maidroid

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView._

package object reminder2
{
    val APP_TAG = "MaidroidReminderFree"

    val AD_PUBLISHER_ID = "a14e9d40ae936a0"
    val AD_TEST_DEVICE_ID_PHONE = "CCDCCBE04AF118FAB9CD4A471243964E"
    val AD_TEST_DEVICE_ID_PAD = "C60E040512A10414ACE5067F0E8B7EE7"

    class OnFocusWrapper(view: View) {
        def setOnFocusListener(action: => Any) {

            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                def onFocusChange(view: View, hasFocus: Boolean): Unit = {
                    if (hasFocus) {
                        action
                    }
                }
            })
        }
    }

    implicit def toOnFocusWrapper(view: View) = new OnFocusWrapper(view)
    implicit def toOnClickListener(action: View => Any) = new View.OnClickListener() {
        def onClick(view: View): Unit = action(view)
    }

    implicit def toFocusChangeListener(action: Boolean => Any) = {
        new View.OnFocusChangeListener() {
            def onFocusChange(view: View, hasFocus: Boolean): Unit = action(hasFocus) 
        }
    }

    implicit def convertToOnItemLongClickListener (action: Int => Boolean) = {
        new OnItemLongClickListener() {
            override def onItemLongClick(parent: AdapterView[_], view: View, 
                                         position: Int, id: Long) = 
            {
                action(position)
            }
        }
    }

    implicit def convertToOnItemClickListener (action: Int => Any) = {
        new OnItemClickListener() {
            override def onItemClick(parent: AdapterView[_], view: View, 
                                     position: Int, id: Long)
            {
                action(position)
            }
        }
    }

    implicit def convertToOnItemSelectedListenerByView (action: View => Any) = {
        new OnItemSelectedListener () {

            override def onNothingSelected(parent: AdapterView[_]) {}
            override def onItemSelected(parent: AdapterView[_], view: View, 
                                        position: Int, id: Long) 
            {
                action(view)
            }
        }
    }

    implicit def convertToOnItemSelectedListener (action: Int => Any) = {
        new OnItemSelectedListener () {

            override def onNothingSelected(parent: AdapterView[_]) {}
            override def onItemSelected(parent: AdapterView[_], view: View, 
                                        position: Int, id: Long) 
            {
                action(position)
            }
        }
    }

}

