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

import android.content.Context

import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils._

import android.view.View
import android.view.MotionEvent

import android.widget.ScrollView
import android.widget.TextView
import android.widget.ImageView

import android.util.AttributeSet
import android.util.Log
import android.text.method.LinkMovementMethod
import android.text.Html
import android.text.Spannable

import android.text.util.Linkify

class ViewMessageBox (context: Context, attrs: AttributeSet) extends 
      ScrollView     (context, attrs)
{
    type MaidView = {def setImageResource(resID: Int)}

    // Call Initialzation when construct
    init () 

    /*=================================================================
     * Private Fields
     *===============================================================*/
    private lazy val MAX_OFFSET = 5
    private lazy val textView = new TextView (context)
    private var      messageList: List[Message] = Nil
    private var      maidView: Option[MaidView] = None

    def setMaidView (maidView: MaidView) {
        this.maidView = if (maidView != null) Some(maidView) else None
    }

    /*=================================================================
     * Initialization Routine
     *===============================================================*/
    private def init ()
    {
        addView (textView)

        textView.setTextSize (20)
        textView.setPadding (0, 0, 7, 0)
        textView.setTextColor (textView.getTextColors.getDefaultColor)
        textView.setOnClickListener (new View.OnClickListener() {
            override def onClick (view: View) {
                showNextMessage ()
            }
        })
        
        this.setOnTouchListener (onTouchListener)
    }

    /*=================================================================
     * Private Methods
     *===============================================================*/

    private def switchToMessage (message: Message, next: List[Message])
    {
        def setupLinks ()
        {
            val phoneRegex = java.util.regex.Pattern.compile("(\\+[0-9]+[\\- \\.]*)?" + 
                                                        "(\\([0-9]+\\)[\\- \\.]*)?"+
                                                        "([0-9][0-9\\- \\.][0-9\\- \\.]+[0-9])");

            val mailRegex = java.util.regex.Pattern.compile(
                                "[a-zA-Z0-9\\+\\.\\_\\%\\-]{1,256}" +
                                "\\@" +
                                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                                "(" +
                                    "\\." +
                                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                                ")+");

            val webRegex = java.util.regex.Pattern.compile(
                    "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                    + "((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+"   // named host
                    + "(?:"   // plus top level domain
                    + "(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])"
                    + "|(?:biz|b[abdefghijmnorstvwyz])"
                    + "|(?:cat|com|coop|c[acdfghiklmnoruvxyz])"
                    + "|d[ejkmoz]"
                    + "|(?:edu|e[cegrstu])"
                    + "|f[ijkmor]"
                    + "|(?:gov|g[abdefghilmnpqrstuwy])"
                    + "|h[kmnrtu]"
                    + "|(?:info|int|i[delmnoqrst])"
                    + "|(?:jobs|j[emop])"
                    + "|k[eghimnrwyz]"
                    + "|l[abcikrstuvy]"
                    + "|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])"
                    + "|(?:name|net|n[acefgilopruz])"
                    + "|(?:org|om)"
                    + "|(?:zzz|ccc)"
                    + "|(?:pro|p[aefghklmnrstwy])"
                    + "|qa"
                    + "|r[eouw]"
                    + "|s[abcdeghijklmnortuvyz]"
                    + "|(?:tel|travel|t[cdfghjklmnoprtvwz])"
                    + "|u[agkmsyz]"
                    + "|v[aceginu]"
                    + "|w[fs]"
                    + "|y[etu]"
                    + "|z[amw]))"
                    + "|(?:(?:25[0-5]|2[0-4]" // or ip address
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]"
                    + "|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9])))"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~"  // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)"); // and finally, a word boundary or end of
                                    // input.  This is to stop foo.sure from
                                    // matching as foo.su

            Linkify.addLinks (textView, phoneRegex, "tel://", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter)
            Linkify.addLinks (textView, mailRegex, "mailto:")
            Linkify.addLinks (textView, webRegex, "", Linkify.sUrlMatchFilter, null)

            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        def updateTextView (message: Message)
        {
            val ARROW     = "\u25bc"
            val DIAMOND   = "\u25c6"
            val endSymbol = if (next == Nil) DIAMOND else ARROW

            Log.e ("qqq", "message.message:" + message.message)
            val s = Html.fromHtml(message.message.replaceAll("\n","<br/>") + endSymbol)

            textView.setText (s)
            setupLinks ()
        }

        def prepareAnimation (): Animation =
        {
            val fadeIn  = loadAnimation (context, android.R.anim.fade_in)
            val fadeOut = loadAnimation (context, android.R.anim.fade_out)

            fadeOut.setAnimationListener (new AnimationListener () {
                def onAnimationEnd (animation: Animation) {
                    updateTextView (message)
                    textView.startAnimation (fadeIn)
                }

                def onAnimationRepeat (animation: Animation) {}
                def onAnimationStart  (animation: Animation) {}
            })

            fadeOut
        }

        val animation = prepareAnimation ()
        textView.startAnimation (animation)

        for (imageView <- maidView) {
            imageView.setImageResource (message.faceResID)
        }

        message.play (context)
    }

    /**
     *  用來檢查是否有 ScrollBar 出現，測試的標準是當 TextView 比
     *  ScrollView 來的大時，就當做有 ScrollBar。
     */
    private def hasScrollBar = textView.getHeight > this.getHeight

    /*=================================================================
     * Callbacks
     *===============================================================*/

    /**
     *  重點在這裡。
     *
     *  Click 的定義：壓下與放開的點誤差不超過 1
     */
    private lazy val onTouchListener = new View.OnTouchListener ()
    {
        // 壓下起始座標
        var startX = 0.0
        var startY = 0.0

        def onTouch (view: View, event: MotionEvent): Boolean = {

            // 如果沒有 ScrollBar 出現，除了 ACTION_DOWN 之外
            // 的 Event 都會被吃掉，所以直接當做 Click 處理。
            if (!hasScrollBar) {
                Log.e ("Maidroid", "==> There is no scroll bar");
                showNextMessage ()
                return false
            }

            Log.e ("Maidroid", "==> event.getAction = " + event.getAction);
            Log.e ("Maidroid", "==> event.getX = " + event.getX);
            Log.e ("Maidroid", "==> event.getY = " + event.getY);

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

    /*=================================================================
     * Public Field / Method
     *===============================================================*/

    // 當最後一個訊息被顯示時會呼叫這個函式
    var injectWhenLast: () => Unit = () => {}

    def setMessageList (messageList:List[Message])
    {
        setMessageList (messageList, false)
    }

    def setMessageList (messageList:List[Message], append: Boolean)
    {
        if (append == true) {
            this.messageList :::= messageList
        } else {
            this.messageList = messageList
            showNextMessage ()
        }
    }

    def showNextMessage ()
    {
        // 將 TextView 設成 List 裡的第一個字串
        messageList match {
            case message :: next =>  
                switchToMessage (message, next)

                if (next == Nil) {
                    injectWhenLast ()
                }

                messageList = next

            case _ =>
        }
    }
}
