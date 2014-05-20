package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.common.Full
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

class Boot {

    // Global Setup for Lift Web Application
    def boot {
        LiftRules.addToPackages ("code")

        val entries = Menu(Loc("Home", "index" :: Nil, "首頁")) :: 
                      Menu(Loc("View", "view"  :: Nil, "檢視")) :: Nil

        LiftRules.ajaxPostTimeout = 1000 * 60 * 15
        LiftRules.setSiteMap(SiteMap(entries:_*))
    }
}
