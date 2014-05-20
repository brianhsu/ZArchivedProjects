package org.maidroid.reminder2

import org.maidroid.calendar.model.Event

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

import scala.collection.JavaConversions._
import com.google.ical.compat.javautil.DateIteratorFactory

import java.util.Date
import java.util.TimeZone

object AndroidDBEvent
{
    implicit def convertFromEvent(event: Event) = new AndroidDBEvent(event)
    implicit def convertToEvent(event: AndroidDBEvent) = event.event

    def apply(context: Context, intent: Intent) = {

        val calendarDAO = Calendar.getCalendarDAO(context)
        val uri = intent.getData
        val calendarID = uri.getPath.split("/")(1).toLong // intent.getLongExtra("CalendarID", -1)
        val eventID    = uri.getPath.split("/")(2).toLong // intent.getLongExtra("EventID", -1)

        val events = calendarDAO.getCalendar(calendarID).get.getEvents
     
        new AndroidDBEvent (events.dropWhile (_.eventID != eventID).head)
    }
}

class AndroidDBEvent(val event: Event)
{
    lazy val androidURIString = "mevent://androidDB/%d/%d" format(event.calendarID, event.eventID)
    private def setAlarm(context: Context, alarmTime: Date)
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

        val broadcastIntent =
        {
            val requestCode = 0 // Not Used
            val flags  = 0 // Flags
            val uri    = Uri.parse(androidURIString + "/" + alarmTime.getTime)
            val intent = new Intent ("org.maidroid.reminder2.RECEIVE_REMIND", uri)

            Log.i(APP_TAG, "setAlarm:" + uri)
            PendingIntent.getBroadcast (context, requestCode, intent, flags)
        }

        alarmManager.set (AlarmManager.RTC_WAKEUP, alarmTime.getTime, broadcastIntent)
    }

    private def unsetAlarm(context: Context, alarmTime: Date)
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

        val broadcastIntent =
        {
            val requestCode = 0 // Not Used
            val flags  = PendingIntent.FLAG_UPDATE_CURRENT // Flags
            val uri    = Uri.parse(androidURIString + "/" + alarmTime.getTime)
            val intent = new Intent ("org.maidroid.reminder2.RECEIVE_REMIND", uri)

            Log.i(APP_TAG, "unsetAlarm:" + uri)
            PendingIntent.getBroadcast (context, requestCode, intent, flags)
        }

        alarmManager.cancel (broadcastIntent)
    }

    def unregisterAlarm (context: Context)
    {
        val reminders = event.getReminders
        var thread = new Thread () {
            override def run () {
                val reminders = event.getReminders
    
                event.recurrence match {
                    case None => 
                        reminders.foreach { alarmTime => unsetAlarm(context, alarmTime) }

                    case Some(repeatNotation) if reminders != Nil =>

                        reminders.tail.foreach { alarmTime => unsetAlarm(context, alarmTime) }

                        val repeatAlarms = DateIteratorFactory.createDateIterator(
                            repeatNotation, reminders.head, TimeZone.getTimeZone("GMT"), false
                        )

                        // No one never reboot his Android Phone in half year, right?
                        val futureAlarms = repeatAlarms.take(200)
                        futureAlarms.foreach {alarmTime => unsetAlarm(context, alarmTime)}

                    case _ =>
                }
            }
        }

        thread.start()
    }

    def registerAlarm (context: Context)
    {
        var thread = new Thread () {
            override def run () {
                val currentTimestamp = (new Date).getTime
                def isFuture(time: Date) = time.getTime > currentTimestamp
                val reminders = event.getReminders
    
                event.recurrence match {
                    case None => 
                        reminders.filter(isFuture).foreach { 
                            alarmTime => setAlarm(context, alarmTime) 
                        }

                    case Some(repeatNotation) if reminders != Nil =>
                        reminders.tail.filter(isFuture).foreach { 
                            alarmTime => setAlarm(context, alarmTime) 
                        }

                        val repeatAlarms = DateIteratorFactory.createDateIterator(
                            repeatNotation, reminders.head, TimeZone.getTimeZone("GMT"), false
                        )

                        // No one never reboot his Android Phone in half year, right?
                        val futureAlarms = repeatAlarms.filter(isFuture).take(180)
                        futureAlarms.foreach {alarmTime => setAlarm(context, alarmTime)}

                    case _ =>
                }
            }

        }

        thread.start()
    }


    def notification(context: Context, message: Message) = {

        def viewReminderIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(androidURIString))

        val appContext = context.getApplicationContext
        val contentIntent = PendingIntent.getActivity(appContext, 0, viewReminderIntent, 0)
        val setting = new Settings(context)
        val notification = new Notification(
            R.drawable.notification, message.text, 
            System.currentTimeMillis
        )

        notification.flags |= Notification.FLAG_AUTO_CANCEL
        notification.flags |= Notification.FLAG_INSISTENT
        notification.defaults |= Notification.DEFAULT_LIGHTS

        setting.enableVibration match {
            case true  => notification.defaults |= Notification.DEFAULT_VIBRATE
            case false => notification.defaults &= ~Notification.DEFAULT_VIBRATE
        }
        
        setting.enableSound match {
            case true  => 
                message.voice.foreach {voiceURI =>
                    notification.defaults &= ~Notification.DEFAULT_SOUND
                    notification.sound = Uri.parse(voiceURI)
                }

            case false => notification.defaults &= ~Notification.DEFAULT_SOUND
        }

        val contentTitle = event.title
        val contentText = event.description getOrElse ""

        notification.contentIntent = contentIntent
        notification.setLatestEventInfo(appContext, contentTitle, contentText, contentIntent);
        notification
    }
}
