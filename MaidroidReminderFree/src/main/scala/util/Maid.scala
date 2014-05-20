package org.maidroid.reminder2

import org.maidroid.calendar.model.Event

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory
import android.graphics.Bitmap

import java.text.SimpleDateFormat
import scala.xml.XML
import scala.xml.Node

object Maid
{
    var maidMap: Map[Symbol, Maid] = Map()

    def getMaid (context: Context, maidName: Symbol) = maidMap.get(maidName) match {
        case Some(maid) => maid
        case None =>
            val newMaid = new MaroMaid(context)
            maidMap += (maidName -> newMaid)
            newMaid
    }
}

trait Maid {
    val context: Context
    def welcomeMessages: List[Message]
    def addEventMessage: List[Message] 
    def editEventMessage: List[Message]
    def addButtonMessage: List[Message]
    def browseButtonMessage: List[Message]
    def settingButtonMessage: List[Message]
    def exitButtonMessage: List[Message]

    def needTitle: Message
    def timeWrong: Message

    def addEventOK: Message
    def timeArrived: Message
    def deleteConfirm: Message
    def whatShouldIDo: Message

    def preferenceWelcome: List[Message]
    def preferenceVoice: List[Message]
    def preferenceSound: List[Message]
    def preferenceVibration: List[Message]

    def tabWeek: List[Message]
    def tabMonth: List[Message]
    def tabAll: List[Message]
    def tabRepeat: List[Message]
    def tabOutdate: List[Message]
    def tabEventsCleared: List[Message]

    def reminderMessage(event: Event): List[Message]
    def contextMenuMessage(event: Event): List[Message]
    def contextDeleteMessage(event: Event): List[Message]
    def eventDetailMessage(event: Event): List[Message]

    val packageName = "org.maidroid.reminder2"

    var drawable: Map[Int, Bitmap] = Map()

    def getSoundURI(resource: String) = Some("android.resource://%s/raw/%s" format(packageName, resource))

    private def getDrawable(resourceName: String) = {

        val resourceID = context.getResources.getIdentifier(
            resourceName, "drawable", packageName
        )

        drawable.get(resourceID) match {
            case None => 
                val bitmap = BitmapFactory.decodeResource(context.getResources, resourceID)
                drawable += (resourceID -> bitmap)
                bitmap

            case Some(bitmap) => bitmap
        }
    }

    protected def loadMessage(xmlNode: Node) = {
        val text = xmlNode.text.trim
        val drawable = getDrawable((xmlNode \\ "@bitmap").text.trim)
        val soundURI: Option[String] = (xmlNode \\ "@soundURI").map(_.text.trim) match {
            case Nil => None
            case sound :: _ => getSoundURI(sound)
        }

        Message(text, drawable, soundURI)
    }

    protected def loadEventMessage(event: Event, xmlNode: Node) = {

        val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
        val timeFormatter = new SimpleDateFormat("HH:mm")

        val date = dateFormatter.format(event.startTime)
        val time = timeFormatter.format(event.startTime)

        def applyData(s: String) = {
            s.replaceAll("\\{title\\}", event.title).
              replaceAll("\\{description\\}", event.description getOrElse "").
              replaceAll("\\{date\\}", date).
              replaceAll("\\{time\\}", time)
        }

        val message = loadMessage(xmlNode)
        Message(applyData(message.text.stripMargin), message.maidBitmap, message.voice)
    }

}

class MaroMaid(override val context: Context) extends Maid
{

    object Emotion {
        val smile = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_smile)
        val happy = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_happy)
        val panic = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_panic)
        val shy   = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_shy)
        val anger = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_anger)
    }

    object EmotionHalf {
        val smile = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_h_smile)
        val happy = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_h_happy)
        val panic = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_h_panic)
        val shy   = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_h_shy)
        val anger = BitmapFactory.decodeResource(context.getResources, R.drawable.maro_h_anger)
    }

    val maroXML = XML.load(context.getResources.openRawResource(R.raw.maro_maid))

    def welcomeMessages = (maroXML \ "Welcome" \\ "Message").map(loadMessage).toList
    def addEventMessage = (maroXML \ "AddEvent" \\ "Message").map(loadMessage).toList
    def editEventMessage = (maroXML \ "EditEvent" \\ "Message").map(loadMessage).toList

    def addButtonMessage = (maroXML \ "AddButton" \\ "Message").map(loadMessage).toList
    def browseButtonMessage = (maroXML \ "BrowseButton" \\ "Message").map(loadMessage).toList
    def settingButtonMessage = (maroXML \ "SettingButton" \\ "Message").map(loadMessage).toList
    def exitButtonMessage = (maroXML \ "ExitButton" \\ "Message").map(loadMessage).toList

    def needTitle = (maroXML \ "NeedTitle").map(loadMessage).head
    def timeWrong = (maroXML \ "TimeWrong").map(loadMessage).head

    def addEventOK  = (maroXML \ "AddEventOK").map(loadMessage).head
    def timeArrived = (maroXML \ "TimeArrived").map(loadMessage).head
    def deleteConfirm = (maroXML \ "DeleteConfirm").map(loadMessage).head
    def whatShouldIDo = (maroXML \ "WhatShouldIDo").map(loadMessage).head

    def preferenceWelcome = (maroXML \ "PreferenceWelcome" \\ "Message").map(loadMessage).toList
    def preferenceVoice = (maroXML \ "PreferenceVoice" \\ "Message").map(loadMessage).toList
    def preferenceSound = (maroXML \ "PreferenceSound" \\ "Message").map(loadMessage).toList
    def preferenceVibration = (maroXML \ "PreferenceVibration" \\ "Message").map(loadMessage).toList

    def tabWeek = (maroXML \ "TabWeek" \\ "Message").map(loadMessage).toList
    def tabMonth = (maroXML \ "TabMonth" \\ "Message").map(loadMessage).toList
    def tabAll = (maroXML \ "TabAll" \\ "Message").map(loadMessage).toList
    def tabRepeat = (maroXML \ "TabRepeat" \\ "Message").map(loadMessage).toList
    def tabOutdate = (maroXML \ "TabOutdated" \\ "Message").map(loadMessage).toList
    def tabEventsCleared = (maroXML \ "TabEventsCleared" \\ "Message").map(loadMessage).toList

    def reminderMessage(event: Event) = {
        (maroXML \ "ReminderMessage" \\ "Message").map(loadEventMessage(event, _)).toList
    }

    def contextMenuMessage(event: Event) = {
        (maroXML \ "ContextMenuMessage" \\ "Message").map(loadEventMessage(event, _)).toList
    }

    def contextDeleteMessage(event: Event) = {
        (maroXML \ "ContextDeleteMessage" \\ "Message").map(loadEventMessage(event, _)).toList
    }

    def eventDetailMessage(event: Event) = {
        (maroXML \ "EventDetailMessage" \\ "Message").map(loadEventMessage(event, _)).toList
    }

}

