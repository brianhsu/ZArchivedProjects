package code.snippet

import net.liftweb.util._ 
import Helpers._
import net.liftweb.util.BindHelpers
import net.liftweb.util.BindHelpers._
import net.liftweb.http.TemplateFinder
import net.liftweb.http.SHtml
import net.liftweb.http.SessionVar
import net.liftweb.http.S

import scala.xml.NodeSeq
import scala.xml._

import org.bone.splurk.model.Plurk
import org.bone.splurk.model.PlurkUser
import code.model.PlurkBackup


object MyPlurks extends SessionVar[List[Plurk]](Nil)
object AllPlurks extends SessionVar[List[Plurk]](Nil)
object AllUsers extends SessionVar[Map[Long, PlurkUser]](Map())
object BackupAll extends SessionVar[Boolean](false)
object ProcessTag extends SessionVar[Boolean](false)
object Username extends SessionVar[String]("")
object Password extends SessionVar[String]("")

class BackupUI
{
    lazy val (allUsers, allPlurks, myPlurks) = PlurkBackup.fetch(Username.get, Password.get)
    lazy val getAllPlurksHTML = convertPlurksToHTML(AllPlurks.get, "所有的噗")
    lazy val getMyPlurksHTML = convertPlurksToHTML(MyPlurks.get, "我的噗")
    lazy val getTags = filterTags(if (BackupAll.get) AllPlurks.get else MyPlurks.get)
    lazy val getTagsTable = getTags.map {tag => 
        val plurks = if (BackupAll.get) AllPlurks.get else MyPlurks.get

        (tag, convertPlurksToHTML(plurks.filter(_.content contains tag), tag))
    }

    def convertPlurksToHTML (plurks: List[Plurk], title: String) = {
        val templateXML = TemplateFinder.findAnyTemplate("templates-hidden" :: "plurks" :: Nil).get
        val template = BindHelpers.chooseTemplate("backup", "table", templateXML)

        val rows = plurks.flatMap {plurk=>
            val userInfo = AllUsers(plurk.ownerID)
            val dateWithLink = <a href={plurk.plurkURL}>{PlurkBackup.dateFormatter.format(plurk.posted)}</a>
            val time = PlurkBackup.timeFormatter.format(plurk.posted)
            val userURL = "http://plurk.com/" + userInfo.nickName
            val username = userInfo.displayName.getOrElse(userInfo.fullName.getOrElse(userInfo.nickName))
            val userLink = <a href={userURL}>{username}</a>
            val qualifier = plurk.qualifierTranslated.getOrElse(plurk.qualifier.text)
            val content = plurk.content
            val contentHTML = scala.xml.XML.loadString("<td>" + plurk.content + "</td>");
            val responseCount = <a href={plurk.plurkURL}>{plurk.responseCount}</a>

            BindHelpers.bind ("backup", template,
                "date" -> dateWithLink,
                "time" -> time,
                "nickname" -> userLink,
                "responseCount" -> responseCount,
                "qualifier" -> qualifier,
                "content" -> contentHTML
            )
        }

        BindHelpers.bind (
            "backup", templateXML, 
            "title" -> <a name={title}>{title}</a>,
            "table" -> rows
        )
    }

    def filterTags(plurks: List[Plurk]) = {
        import scala.util.matching.Regex

        val Tag = new Regex("""\[[^\p{Punct}]*\]""")
        plurks.flatMap(plurk => (Tag findAllIn plurk.content).toSet).distinct
    }

    def convertToTagLink(title: String) = <li><a href={"#"+title}>{title}</a></li>

    def refresh = {
        def refreshBackup() {
            AllUsers(Map())
            AllPlurks(Nil)
            MyPlurks(Nil)
            
            S.redirectTo ("/view")                    
        }

        "#refresh" #> SHtml.submit("重新整理", refreshBackup _)
    }

    def view = {

        try {

            if (Username.get == "" || Password.get == "") {
                throw new Exception("請先輸入帳號密碼")
            }

            if (AllUsers.get == Map()) AllUsers(this.allUsers)
            if (AllPlurks.get == Nil) AllPlurks(this.allPlurks)
            if (MyPlurks.get == Nil) MyPlurks(this.myPlurks)

            val allPlurksTag = if (BackupAll.get) List("所有的噗") else Nil
            val othersTag = if (ProcessTag.get) getTags else Nil
            val tags = List("我的噗") ++ allPlurksTag ++ othersTag

            val tagsLink = tags.map(convertToTagLink)

            val allPlurks = if (BackupAll.get) getAllPlurksHTML else <div/>
            val keywordsPlurks = if (ProcessTag.get) getTagsTable.flatMap(_._2) else <div/>

            "#tags" #> tagsLink &
            "#table" #> (getMyPlurksHTML ++ allPlurks ++ keywordsPlurks)
        } catch {
            case e: Exception => 
                S.error("錯誤：" + e.getMessage)

                "#tags" #> <div/> &
                "#table" #> <div/>
        }
    }

    def login = {

        def processBackup() {
            AllUsers(Map())
            AllPlurks(Nil)
            MyPlurks(Nil)
            
            S.redirectTo ("/view")                    
        }

        "#username"   #> SHtml.text(Username.get, Username(_), "id" -> "username") &
        "#password"   #> SHtml.password("", Password(_), "id" -> "password") &
        "#backupAll"  #> SHtml.checkbox(BackupAll.get, BackupAll(_), "id" -> "backupAll") &
        "#processTag" #> SHtml.checkbox(ProcessTag.get, ProcessTag(_), "id" -> "processTag") &
        "#submit"     #> SHtml.submit("備份", processBackup)
    }
}
