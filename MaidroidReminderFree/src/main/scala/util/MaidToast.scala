package org.maidroid.reminder2

import android.app.Activity

import android.view.Gravity
import android.view.ViewGroup.{LayoutParams => VGParams}

import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView

trait MaidToast extends Activity
{
    protected val maid: Maid

    type Assertion = (() => Boolean, Message)

    def assertWith[T] (assertions: Assertion*)(action: => T):Either[Message, T] = {
        
        val result = assertions.map (x => (x._1(), x._2)).dropWhile(_._1 == true).toList

        result match {
            case Nil => Right(action)
            case (status, message) :: _ => 
                showToast(message)
                Left(message)
        }
            
    }

    def showToast (message: Message)
    {
        val toast     = new Toast (this)
        val textView  = new TextView (this)
        val imageView = new ImageView (this)
        val layout    = new LinearLayout (this)

        textView.setText (message.text)
        textView.setTextSize (20)
        textView.setGravity (Gravity.CENTER)

        imageView.setImageBitmap (message.maidBitmap)

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
        toast.setDuration (Toast.LENGTH_SHORT)

        toast.show ()
    }

}

