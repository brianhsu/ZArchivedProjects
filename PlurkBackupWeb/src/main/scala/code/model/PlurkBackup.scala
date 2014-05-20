package code.model

import org.bone.splurk._
import org.bone.splurk.constant._
import org.bone.splurk.model._
import java.util.Date
import java.text.SimpleDateFormat

object PlurkBackup
{
    val apiKey = "wQsqUmmcp69aCblemZ34pM5wywnlAyS9"
    val plurkClient = new PlurkClient(apiKey)
    val dateFormatter = new SimpleDateFormat ("yyyy-MM-dd")
    val timeFormatter = new SimpleDateFormat ("hh:mm:ss")

    var allUsers: Map[Long, PlurkUser] = Map()

    // 遞迴取得河道上所有的噗文
    def fetchPlurks (dateBound: Date, filter: Option[PlurkFilter.PlurkFilter] = None): List[Plurk] = {
        val (users, plurks) = plurkClient.Timeline.getPlurks (limit = 10,
                                                              olderThan = Some(dateBound), 
                                                              filter = filter)
        allUsers = allUsers ++ users

        plurks match {
            case Nil => Nil
            case xs: List[_] =>
                println ("Chunk:" + xs.length)
                xs.foreach {x => println ("Processing plurkID %d..." format(x.plurkID)) }
                fetchPlurks(xs.last.posted, filter) ++ xs
        }
    }

    def fetch (username: String, password: String) = {
        val profile   = plurkClient.Users.login (username, password)
        val allPlurks = fetchPlurks (new Date)

        val allPlurksBackup:List[Plurk] = allPlurks.sortWith(_.posted.getTime > _.posted.getTime)
        val myPlurksBackup = allPlurks.sortWith(_.posted.getTime > _.posted.getTime).
                                       filter(_.ownerID == profile.userInfo.userID)

        (allUsers, allPlurksBackup, myPlurksBackup)
    }
}

