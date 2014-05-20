package org.maidroid.reminder2

import android.preference.PreferenceManager
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.view.View
import android.widget.TextView
import android.widget.ScrollView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils._
import android.util.Log
import android.view.MotionEvent

trait MaidDialogUI
{
    def context: Context
    def messageBox: TextView
    def maidImage: ImageView
    def scrollView: ScrollView
}

class MaidDialog(activity: MaidDialogUI)
{ 
    var messages: List[Message] = Nil

    initMessageBox()

    activity.maidImage.setOnClickListener(new View.OnClickListener() {
        override def onClick (view: View) {
            showNextMessage()
        }
    })
    
    def setMessageText (message: Message, isLastMessage: Boolean) {

        val DECORATION = isLastMessage match {
            case true   => "" // Diamond
            //case true   => "\u25c6" // Diamond
            case false  => "\u25bc" // Arrow
        }

        def prepareAnimation (): Animation =
        {
            val fadeIn  = loadAnimation (activity.context, android.R.anim.fade_in)
            val fadeOut = loadAnimation (activity.context, android.R.anim.fade_out)

            fadeOut.setAnimationListener (new AnimationListener () {
                def onAnimationEnd (animation: Animation) {
                    activity.messageBox.setText (message.text + DECORATION)
                    activity.messageBox.startAnimation (fadeIn)
                }

                def onAnimationRepeat (animation: Animation) {}
                def onAnimationStart  (animation: Animation) {}
            })

            fadeOut
        }

        val animation = prepareAnimation ()

        activity.messageBox.startAnimation (animation)
        activity.maidImage.setImageBitmap(message.maidBitmap)

        val settings = new Settings(activity.context);
        message.play(activity.context)
    }

    def showNextMessage () {

        messages match {
            case current :: Nil =>
               setMessageText (current, true)
               messages = messages.tail

            case current :: remains =>
               setMessageText (current, false)
               messages = messages.tail
               
            case Nil =>
        }
    }

    def initMessageBox () {

        val onTouchListener = new View.OnTouchListener ()
        {
            val MAX_OFFSET = 20

            // 壓下起始座標
            var startX = 0.0
            var startY = 0.0

            private def hasScrollBar = activity.messageBox.getHeight > 
                                       activity.scrollView.getHeight

            private var previousTime = 0L

            def onTouch (view: View, event: MotionEvent): Boolean = {


                // 如果沒有 ScrollBar 出現，除了 ACTION_DOWN 之外
                // 的 Event 都會被吃掉，所以直接當做 Click 處理。
                if (!hasScrollBar && event.getAction == MotionEvent.ACTION_DOWN) {
                    showNextMessage ()
                    return false
                }

                // 如果有 ScrollBar，先記錄起始下壓點在哪，如果按下
                // 與放下的點 X 與 Y 軸均不超過 MAX_OFFSET ，就當做
                // 是 Click 事件。
                event.getAction match {
                    case MotionEvent.ACTION_DOWN =>
                        startX = event.getX
                        startY = event.getY

                    case MotionEvent.ACTION_UP =>
                        if ((startX - event.getX).abs < MAX_OFFSET &&
                            (startY - event.getY).abs < MAX_OFFSET) {
                            showNextMessage ()
                        }

                    case _ =>
               }

               return false
            }
        }

        activity.scrollView.setOnTouchListener(onTouchListener)
        activity.messageBox.setTextColor(activity.messageBox.getTextColors.getDefaultColor)
        activity.messageBox.setOnClickListener(new View.OnClickListener() {
            override def onClick (view: View) {
                showNextMessage()
            }
        })
    }

    // Public Methods
    def setMessages (messages: List[Message]) {
        this.messages = messages
        showNextMessage ()
    }

}

