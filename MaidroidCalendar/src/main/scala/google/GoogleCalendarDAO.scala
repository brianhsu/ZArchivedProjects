package org.maidroid.calendar.model

import com.google.api.client.http.InputStreamContent
import java.util.Date
import scala.xml.NodeSeq
import scala.xml.XML
import scala.xml.Elem
import scala.xml.transform.RewriteRule
import scala.xml.transform.RuleTransformer
import scala.xml.Node

import scala.io.Source
import scala.util.parsing.json.JSON
import java.text.SimpleDateFormat
import java.util.Locale

case class GoogleCalendarDAO(username: String, password: String) extends CalendarDAO
{
    private val calendarProtocol = new GCalendarProtocol (username, password, "Maidroid-Reminder-0")
    private lazy val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH)
    private lazy val dateFormatterXML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)
    private lazy val dateFormatterOnlyDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    def login () = calendarProtocol.login()

    object URI {
        val addCalendar  = "https://www.google.com/calendar/feeds/default/owncalendars/full"
        val getCalendars = "https://www.google.com/calendar/feeds/default/owncalendars/full"

        def getCalendarUpdateURL (calendar: Calendar) = {
            val calendarName = calendar.calendarURL.split("/").last
           
            "https://www.google.com/calendar/feeds/default/owncalendars/full/%s" format(calendarName)
        }

        def getCreateEventURL (calendar: Calendar) = {
            val calendarName = calendar.calendarURL.split("/").last
           
            "https://www.google.com/calendar/feeds/%s/private/full" format(calendarName)
        }

        def getEventsFeedURL (calendar: Calendar) = {
            val calendarName = calendar.calendarURL.split("/").last
           
            "https://www.google.com/calendar/feeds/%s/private/full?start-min=1970-01-01" format(calendarName)
        }

        def getEventUpdateURL (event: Event) = event.eventURL
        
    }

    private def getXML (url: String) = {
        val response = calendarProtocol.transport.buildGetRequest(url).execute()

        XML.load(response.getContent)
    }

    def createNewCalendar (title: String, description: Option[String] = None): Calendar = {


        def xmlCalendar = <entry xmlns='http://www.w3.org/2005/Atom'
                                 xmlns:gd='http://schemas.google.com/g/2005'
                                 xmlns:gCal='http://schemas.google.com/gCal/2005'>

                              <title type='text'>{title}</title>
                              <summary type='text'>{description getOrElse ""}</summary>
                              <gCal:hidden value='false'></gCal:hidden>
                          </entry>

        def executeUpdate () = {
            val request = calendarProtocol.transport.buildPostRequest(URI.addCalendar)

            request.request.content = new InputStreamContent
            request.request.content.asInstanceOf[InputStreamContent].`type` = "application/atom+xml"
            request.request.content.asInstanceOf[InputStreamContent].
                                    setByteArrayInput(xmlCalendar.toString.getBytes("UTF-8"))

            val response = request.execute()
            val calendarURL = (XML.load(response.getContent) \\ "id").text

            calendarURL
        }

        val calendarURL = try {
                              executeUpdate ()
                          } catch {
                              case e => executeUpdate ()
                          }

        getCalendar(calendarURL.hashCode).get
    }

    def getCalendar(calendarID: Long): Option[Calendar] = {
        getCalendars.filter(_.calendarID == calendarID) match {
            case Nil => None
            case calendar :: xs => Some(calendar)
        }
    }

    def getCalendars(): List[Calendar] = {
        val response = calendarProtocol.transport.buildGetRequest(URI.getCalendars+"?alt=jsonc").execute()
        val jsonText = Source.fromInputStream(response.getContent).mkString
        val jsonParsed = JSON.parseFull(jsonText).get.asInstanceOf[Map[String, Any]]("data").
                                                      asInstanceOf[Map[String, Any]]

        val jsonCalendars = jsonParsed("items").asInstanceOf[List[Map[String, String]]]

        def convertJSONToCalendar(json: Map[String, String]) = {
            val created = dateFormatter.parse(json("created"))
            val updated = dateFormatter.parse(json("updated"))
            val calendarID = json("id").hashCode
            val description = json.get("details").map(_.toString.trim) match {
                case None     => None
                case Some("") => None
                case other    => other
            }

            Calendar (
                calendarDAO = this, 
                calendarID  = calendarID,
                calendarURL = json("id"),
                title       = json("title"), 
                description = description,
                lastUpdated = updated
            )
        }

        jsonCalendars.map(convertJSONToCalendar)
    }

    def getEvents (calendar: Calendar): List[Event] = {
        val xml = getXML(URI.getEventsFeedURL(calendar))

        def convertToEvent (xml: NodeSeq) = {

            def hasNoEndTime (x: Node) = (x \\ "@endTime" == NodeSeq.Empty)
            def sortByStartTime (x: Node, y: Node) = {
                convertToDate(x.attributes("startTime")(0)).getTime < 
                convertToDate(y.attributes("startTime")(0)).getTime
            }

            def convertToDate(node: Node) = node.text match {
                case time if time.length == 10 => dateFormatterOnlyDate.parse(time)
                case time if time.length == 24 => dateFormatter.parse(time)
                case time                      => dateFormatterXML.parse(time.take(26) + time.drop(27))
            }

            val when = (xml \\ "when").filterNot(hasNoEndTime).sortWith(sortByStartTime)(0)
            val startTime = convertToDate(when.attribute("startTime").get(0))
            val endTime = (when.attribute("endTime").get(0).text) match {
                case time if time.length == 10 => None
                case time if time.length == 24 => Some(dateFormatter.parse(time))
                case time                      => Some(dateFormatterXML.parse(time.take(26) + time.drop(27)))
            }

            def getEventURL = (xml \\ "link").filter(x => (x \\ "@rel").text == "edit")(0).
                                              attributes("href").text

            def getRecurrence = (xml \\ "recurrence").map(_.text).toList match {
                case Nil => None
                case recurrence :: xs => Some(recurrence)
            }

            Event(
                calendarDAO = this,
                eventID = (xml \\ "id").text.hashCode,
                calendarID = calendar.calendarID,
                eventURL = getEventURL,
                title = (xml \\ "title").text,
                startTime = startTime,
                isFullDay = endTime == None,
                endTime = endTime,
                description = (xml \\ "content").text match {case null => None; case text => Some(text)},
                recurrence = getRecurrence
            )
        }

        (xml \\ "entry").map(convertToEvent).toList.sortWith (_.startTime.getTime < _.startTime.getTime)
    }

    def getEvent(eventID: Long): Option[Event] = {
        getCalendars.flatMap(getEvents(_)).filter(_.eventID == eventID) match {
            case Nil => None
            case event :: xs => Some(event)
        }
    }

    def saveCalendar (newCalendar: Calendar) {
        val xml =  <entry xmlns='http://www.w3.org/2005/Atom'
                          xmlns:gCal='http://schemas.google.com/gCal/2005'
                          xmlns:gd='http://schemas.google.com/g/2005'>
                     <id>{newCalendar.calendarURL}</id>
                     <updated>{dateFormatter.format(new Date)}</updated>
                     <title type='text'>{newCalendar.title}</title>
                     <summary type='text'>{newCalendar.description getOrElse null}</summary>
                   </entry>

        
        val updateURL = URI.getCalendarUpdateURL(newCalendar)

        val request = calendarProtocol.transport.buildPutRequest(updateURL)
        request.request.content = new InputStreamContent
        request.request.content.asInstanceOf[InputStreamContent].`type` = "application/atom+xml"
        request.request.content.asInstanceOf[InputStreamContent].
                                setByteArrayInput(xml.toString.getBytes("UTF-8"))

        request.execute()
    }

    def xmlFromEvent (event: Event) = {
        val startTimeXML = event.isFullDay match {
            case true  => dateFormatterOnlyDate.format(event.startTime)
            case false => dateFormatter.format(event.startTime)
        }

        val endTimeXML = event.isFullDay match {
            case true  => dateFormatterOnlyDate.format(event.startTime)
            case false => dateFormatter.format(event.endTime.get)
        }

        val recurrenceXML = event.recurrence match {
            case None => NodeSeq.Empty
            case Some(xml) => XML.loadString(xml)
        }

        val xml = <entry xmlns="http://www.w3.org/2005/Atom" xmlns:gd="http://schemas.google.com/g/2005">
                    <category scheme="http://schemas.google.com/g/2005#kind"
                              term="http://schemas.google.com/g/2005#event" />
                    <title type="text">{event.title}</title>
                    <content type="text">{event.description getOrElse ""}</content>
                    <gd:when startTime={startTimeXML} endTime={endTimeXML}>{recurrenceXML}</gd:when>
                    <gd:transparency value='http://schemas.google.com/g/2005#event.opaque'></gd:transparency>
                  </entry>

    }

    def createNewEvent (calendar: Calendar, title: String, startTime: Date, isFullDay: Boolean,
                        endTime: Option[Date] = None,
                        description: Option[String] = None,
                        recurrence: Option[String] = None): Event = {

        val startTimeXML = isFullDay match {
            case true  => dateFormatterOnlyDate.format(startTime)
            case false => dateFormatter.format(startTime)
        }

        val endTimeXML = isFullDay match {
            case true  => dateFormatterOnlyDate.format(startTime)
            case false => dateFormatter.format(endTime.get)
        }

        val recurrenceXML = recurrence match {
            case None => NodeSeq.Empty
            case Some(text) => <gd:recurrence>{text}</gd:recurrence>
        }
        
        val whenXML = recurrence match {
            case None    => <gd:when startTime={startTimeXML} endTime={endTimeXML}></gd:when>
            case Some(_) => NodeSeq.Empty
        }

        val xml = <entry xmlns="http://www.w3.org/2005/Atom" xmlns:gd="http://schemas.google.com/g/2005">
                    <category scheme="http://schemas.google.com/g/2005#kind"
                              term="http://schemas.google.com/g/2005#event" />
                    <title type="text">{title}</title>
                    <content type="text">{description getOrElse ""}</content>
                    {whenXML}
                    {recurrenceXML}
                    <gd:transparency value='http://schemas.google.com/g/2005#event.opaque'></gd:transparency>
                  </entry>

        val updateURL = URI.getCreateEventURL(calendar)

        def executeUpdate () = {
            val request = calendarProtocol.transport.buildPostRequest(updateURL)

            request.request.content = new InputStreamContent
            request.request.content.asInstanceOf[InputStreamContent].`type` = "application/atom+xml"
            request.request.content.asInstanceOf[InputStreamContent].
                                    setByteArrayInput(xml.toString.getBytes("UTF-8"))

            val response = request.execute()
            val eventURL = (XML.load(response.getContent) \\ "id").text

            getEvent(eventURL.hashCode).get
        }
       
        executeUpdate()
    }

    def saveEvent (newEvent: Event) {

        val startTimeXML = newEvent.isFullDay match {
            case true  => dateFormatterOnlyDate.format(newEvent.startTime)
            case false => dateFormatter.format(newEvent.startTime)
        }

        val endTimeXML = newEvent.isFullDay match {
            case true  => dateFormatterOnlyDate.format(newEvent.startTime)
            case false => dateFormatter.format(newEvent.endTime.get)
        }

        object UpdateXMLTransformer extends RuleTransformer(UpdateRule)
        object UpdateRule extends RewriteRule {

            override def transform(xmlNode: Node): Seq[Node] = xmlNode match {

                case Elem(prefix, "title",   attribs, scope, _*) => 
                        <title type="text">{newEvent.title}</title>

                case Elem(prefix, "content", attribs, scope, _*) => 
                        <content type="text">{newEvent.description getOrElse ""}</content>

                case Elem("gd", "when", attribs, scope, _*)  => 
                        <gd:when startTime={startTimeXML} endTime={endTimeXML}>{xmlNode \\ "reminder"}</gd:when>

                case Elem("gd", "recurrence", attribs, scope, _*) =>
                        newEvent.recurrence.map(x => <gd:recurrence>{x}</gd:recurrence>) getOrElse NodeSeq.Empty

                case other => other
            }
        }

        val oldResponse = calendarProtocol.transport.buildGetRequest(newEvent.eventURL).execute()
        val oldXML = XML.load(oldResponse.getContent)
        val newXML = UpdateXMLTransformer(oldXML)
        val request = calendarProtocol.transport.buildPutRequest(newEvent.eventURL)
    
        request.request.content = new InputStreamContent
        request.request.content.asInstanceOf[InputStreamContent].`type` = "application/atom+xml"
        request.request.content.asInstanceOf[InputStreamContent].
                                setByteArrayInput(newXML.toString.getBytes("UTF-8"))
        request.execute()

    }

    def deleteEvent(event: Event): Int = {

        val deleteURL = URI.getEventUpdateURL(event)
        calendarProtocol.transport.buildDeleteRequest(deleteURL).execute()
        1
    }

    def getReminders(event: Event): List[Date] = {

        def reminderToDate (reminder: Node): Date = {
            val offsetInMillSec = (reminder \\ "@minutes").text.toInt * 60 * 1000
            new Date(event.startTime.getTime - offsetInMillSec)
        }

        def isAlert (reminder: Node) = reminder.attribute("method").map(_.text) == Some("alert")

        val response = calendarProtocol.transport.buildGetRequest(event.eventURL).execute()
        val xml = XML.load(response.getContent)
        val reminders = (xml \\ "reminder").filter(isAlert).map(reminderToDate)
       
        reminders.toList.sortWith(_.getTime < _.getTime)
    }

    def addReminder(event: Event, time: Date) = {
        val offsetInMinute = ((event.startTime.getTime - time.getTime) / 1000.0 / 60.0).toInt.toString
        val xmlNewReminder = <gd:reminder minutes={offsetInMinute} method="alert" />

        object UpdateXMLTransformerNormal extends RuleTransformer(UpdateRuleNormal)
        object UpdateRuleRecurrence extends RewriteRule {
            override def transform(xmlNode: Node): Seq[Node] = xmlNode match {
                case Elem(prefix, "entry", attribs, scope, child @ _*) =>
                    Elem(prefix, "entry", attribs, scope, child ++ xmlNewReminder : _*)

                case other => other
            }
        }

        object UpdateXMLTransformerRecurrence extends RuleTransformer(UpdateRuleRecurrence)
        object UpdateRuleNormal extends RewriteRule {

            override def transform(xmlNode: Node): Seq[Node] = xmlNode match {

                case node @ Elem("gd", "when", attribs, scope, _*)  => 
                        val startTime = (node \\ "@startTime").text
                        val endTime   = (node \\ "@endTime").text
                        <gd:when startTime={startTime} endTime={endTime}>
                            {xmlNode \\ "reminder"}
                            {xmlNewReminder}
                        </gd:when>

                case other => other
            }
        }

        val oldResponse = calendarProtocol.transport.buildGetRequest(event.eventURL).execute()
        val oldXML = XML.load(oldResponse.getContent)
        val newXML = event.recurrence match {
            case None    => UpdateXMLTransformerNormal(oldXML)
            case Some(_) => UpdateXMLTransformerRecurrence(oldXML)
        }

        val request = calendarProtocol.transport.buildPutRequest(event.eventURL)
    
        request.request.content = new InputStreamContent
        request.request.content.asInstanceOf[InputStreamContent].`type` = "application/atom+xml"
        request.request.content.asInstanceOf[InputStreamContent].
                                setByteArrayInput(newXML.toString.getBytes("UTF-8"))
        request.execute()

        getEvent(event.eventID).get
    }

    def deleteReminder(event: Event, time: Date) = {
        val offsetInMinute = ((event.startTime.getTime - time.getTime) / 1000.0 / 60.0).toInt.toString
        var modifiedCount = 0

        def shouldDelete (reminder: Node) = {
            val currentOffset = (reminder \\ "@minutes").text
            val currentMethod = (reminder \\ "@method").text

            (currentOffset == offsetInMinute && currentMethod == "alert")
        }

        object UpdateXMLTransformer extends RuleTransformer(UpdateRule)
        object UpdateRule extends RewriteRule {


            override def transform(xmlNode: Node): Seq[Node] = xmlNode match {
                case node @ Elem("gd", "reminder", attribs, scope, _*) if shouldDelete(node) => 
                    NodeSeq.Empty
                case other => other
            }
        }

        val oldResponse = calendarProtocol.transport.buildGetRequest(event.eventURL).execute()
        val oldXML = XML.load(oldResponse.getContent)
        val newXML = UpdateXMLTransformer(oldXML)

        val request = calendarProtocol.transport.buildPutRequest(event.eventURL)
    
        request.request.content = new InputStreamContent
        request.request.content.asInstanceOf[InputStreamContent].`type` = "application/atom+xml"
        request.request.content.asInstanceOf[InputStreamContent].
                                setByteArrayInput(newXML.toString.getBytes("UTF-8"))
        request.execute()

        (oldXML \\ "reminder").filter(shouldDelete).length
    }

    def deleteCalendar (calendar: Calendar) = {
        val deleteURL = URI.getCalendarUpdateURL(calendar)
        calendarProtocol.transport.buildDeleteRequest(deleteURL).execute()

        0
    }
}

