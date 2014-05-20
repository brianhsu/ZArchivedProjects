/*
  MaidroidGTD Desktop

  Copyright (C) 2011  BrianHsu <brianhsu.hsu@gmail.com>
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

*/

package org.maidroid.gtd.desktop

import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Display

import org.eclipse.swt.SWT

import java.util.Properties
import java.security.Security
import javax.mail.Session
import javax.mail.URLName
import javax.mail.Folder
import javax.mail.Part
import javax.mail.internet.MimeMultipart
import scala.io.Source
import java.io.InputStream

import org.maidroid.gtd.model._

import I18N.TR.{tr => _t}
import org.jsoup.Jsoup

object POP3Receiver
{
    def checkMailAtStart ()
    {
        if (Preference.hasPOP3Config && Preference.get("EMailCheckStart") == Some("true")) {
            
            val receiver = new POP3Receiver
            val recieveThread = new receiver.RecieveMailThread(e => e.printStackTrace())
            recieveThread.start()
        }
    }

    def checkMailAtInboxTab()
    {
        val display = Display.getDefault

        class ShowDialogThread(error: Throwable) extends Thread
        {
            override def run ()
            {
                DialogWindow.setMessages(Maid.mailErrorMessage(error))
            }
        }

        val action = (e: Throwable) => display.syncExec(new ShowDialogThread(e))
        val receiver = new POP3Receiver
        val recieveThread = new receiver.RecieveMailThread(action)

        recieveThread.start()
    }

    def hookEMailCheck ()
    {
        val display = Display.getDefault
        def checkInterval = Preference.get("EMailCheckInterval")
        def checkEvery = Preference.get("EMailCheckEvery")

        val timer = new Runnable() {
            override def run () {
                if (Preference.hasPOP3Config && checkInterval != None) {
            
                    val receiver = new POP3Receiver
                    val recieveThread = new receiver.RecieveMailThread(e => e.printStackTrace())
                    recieveThread.start()
                    checkEvery match {
                        case Some("true") => 
                            display.timerExec(checkInterval.get.toInt * 60 * 1000, this)
                        case None =>
                    }
                }
            }
        }

        checkEvery match {
            case Some("true") => display.timerExec(checkInterval.get.toInt * 60 * 1000, timer)
            case None =>
        }
    }
}

class POP3Receiver
{
    case class Mail(from: String, subject: String, content: String)

    val SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    lazy val host = Preference.get("EMailServer").get
    lazy val port = Preference.get("EMailServerPort").get.toInt
    lazy val username = Preference.get("EMailUserName").get
    lazy val password = Preference.get("EMailPassword").get
    lazy val useSSL = Preference.get("EMailSSL") match {
        case Some("true") => true
        case _ => false
    }

    lazy val properties = useSSL match {
        case false => new Properties
        case true =>

            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

            val props = System.getProperties();
            props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.pop3.socketFactory.fallback", "false");
            props.setProperty("mail.pop3.port", port.toString);
            props.setProperty("mail.pop3.socketFactory.port", port.toString);

            props
    }

    lazy val session = Session.getDefaultInstance(properties,null)
    lazy val url = new URLName("pop3", host, port, null, username, password);

    class RecieveMailThread(errorHandler: Throwable => Any) extends Thread 
    {

        class UpdateUIThread (stuff: Stuff) extends Thread
        {
            override def run () 
            {
                MainWindow.inboxTab.addToStuffTable(stuff)
            }
        }

        val display = Display.getDefault

        override def run ()
        {
            try {

                if (Preference.hasPOP3Config != true) {
                    throw new Exception(_t("POP3 Mail server is not configured correctly"))
                }

                val allMails = getMails()
    
                allMails.foreach { mail =>
                    val stuff = Controller.gtd.Stuff.add(mail.subject, Some(mail.content))
                    display.syncExec(new UpdateUIThread(stuff))
                }

            } catch {
                case e => 
                    e.printStackTrace()
                    errorHandler(e)
            }
        }
    }

    def parseMultipart(multipart: MimeMultipart) =
    {
        def stripHTMLTag(content: String) = Jsoup.parse(content).text().toString
        def isMainContent(part: Part) = {
            part.getContentType.contains("text/plain") && 
            part.getDisposition == null
        }

        def isHTMLContent(part: Part) = {
            part.getContentType.contains("text/html") && 
            part.getDisposition == null
        }

        val parts = for (i <- 0 until multipart.getCount) yield multipart.getBodyPart(i)

        parts.foreach { x =>
            println("    === Part ===")
            println("    content-type:" + x.getContentType)
            println("    disposition:" + x.getDisposition)
            println("    content:" + x.getContent)
            println("    ============")
        }

        println("parts:" + parts.map(x => (x.getContentType, x.getDisposition, x)))

        val plainText = parts.filter(isMainContent).map(_.getContent).toList
        val htmlContent = parts.filter(isHTMLContent).map(_.getContent).toList

        plainText match {
            case Nil => 
                println("NoPlainText:" + htmlContent)
                htmlContent.map(x => stripHTMLTag(x.toString)).mkString("\n")
            case _   => 
                println("NormalPlainText:" + plainText)
                plainText.mkString("\n")
        }
    }

    def getMails() : List[Mail]  =
    {
        val store = session.getStore(url)
        store.connect()

        println(_t("==> Start receive mail"))

        val inbox = store.getFolder("INBOX")
        inbox.open(Folder.READ_ONLY)
        val messages = inbox.getMessages().toList
        val allMails = for (message <- messages) yield {
            val subject = message.getSubject
            val from = message.getFrom.mkString(";")
            val contentType = message.getContentType
            val content = message.getContent match {
                case s: String => 
                    println("=== plain text ==")
                    println("subject:" + subject)
                    println("mail contentType:" + contentType)
                    println(s)
                    println("=================")
                    s
                case m: MimeMultipart => 
                    println("=== Multi-part ==")
                    println("subject:" + subject)
                    println("mail contentType:" + contentType)
                    println("preamble:" + m.getPreamble)
                    val s = parseMultipart(m)
                    println("=================")
                    s
                case o: InputStream => 

                    println("=== InputStream ==")
                    println("subject:" + subject)
                    println("mail contentType:" + contentType)
                    println(o)
                    try {
                        println(Source.fromInputStream(o).mkString)
                    } catch {
                        case e => 
                            println(e.getMessage)
                            println(e.getStackTrace.map(_.toString))

                    }
                    println("=================")
                    o.toString

                case o  => 
                    println("=== Others ==")
                    println("subject:" + subject)
                    println("mail contentType:" + contentType)
                    println(o)
                    println("=================")
                    o.toString

            }

            println(_t("==> received mail:") + subject)
            Mail(from, subject, content)
        }
        println(_t("==> Done"))

        inbox.close(false)
        store.close()

        allMails
    }

}
