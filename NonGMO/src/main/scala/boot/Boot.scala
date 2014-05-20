package bootstrap.liftweb

import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.Html5Properties
import net.liftweb.sitemap._
import net.liftweb.common.Full

import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.http.RedirectResponse
import net.liftweb.http.RedirectWithState
import net.liftweb.http.RedirectState

import net.liftweb.http.OkResponse

import net.liftweb.http.SessionVar
import net.liftweb.http.S
import net.liftweb.util.StringHelpers
import scala.util.Try
import org.scribe.builder._
import org.scribe.builder.api._
import org.scribe.model._
import org.scribe.oauth._
import net.liftweb.util.Helpers.tryo
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


object Login
{
  private implicit val formats = DefaultFormats
  object OAuth2State extends SessionVar[Option[String]](None)

  def getUserID(service: OAuthService, code: String, resourceURL: String) = tryo {
    val verifier = new Verifier(code)
    val accessToken = service.getAccessToken(null, verifier)
    val request = new OAuthRequest(Verb.GET, resourceURL)

    service.signRequest(accessToken, request)
    val jsonResponse = request.send.getBody
    val userID = (JsonParser.parse(jsonResponse) \ "id").extractOpt[String]

    userID.get
  }

  object Google {

    private val apiKey = "32168263492-s20ia0f4pl30cu60dnbu099rdqj6uieu.apps.googleusercontent.com"
    private val apiSecret = "Uo3VizoTG4yBGTVp-Q8XrhOT"
    private lazy val callbackURL = S.hostAndPath + "/login/shop/google/auth"

    def getService = (new ServiceBuilder).provider(classOf[Google2Api])
                          .apiKey(apiKey)
                          .apiSecret(apiSecret)
                          .callback(callbackURL)
                          .scope("email")
                          .build()

    def redirect = EarlyResponse { () =>
      val state = StringHelpers.randomString(48)
      OAuth2State.set(Some(state))

      val url = getService.getAuthorizationUrl(null) + "&state=" + state

      Full(RedirectResponse(url))
    }

    def auth = EarlyResponse { () =>
      
      val userID = for {
        state <- S.param("state") if OAuth2State.get.exists(_ == state)
        code <- S.param("code")
        userID <- getUserID(getService, code, "https://www.googleapis.com/oauth2/v1/userinfo")
      } yield {
        "google" + userID
      }

      println("userID:" + userID)

      userID match {
        case Full(userID) => Full(OkResponse())
        case _ => Full(RedirectWithState("/login/", RedirectState(() => S.notice("登入錯誤，請重試"))))
      }
    }

  }

  object Facebook {

    private val apiKey = "475385685883302"
    private val apiSecret = "a06b75144a053b4829974e7156a55df0"
    private lazy val callbackURL = S.hostAndPath + "/login/shop/facebook/auth"

    def getService = (new ServiceBuilder).provider(classOf[FacebookApi])
                                         .apiKey(apiKey)
                                         .apiSecret(apiSecret)
                                         .callback(callbackURL)
                                         .build()
 
    def redirect = EarlyResponse { () =>
      
      val state = StringHelpers.randomString(48)
      OAuth2State.set(Some(state))

      val url = getService.getAuthorizationUrl(null) + "&state=" + state
      Full(RedirectResponse(url))
    }

    def auth = EarlyResponse { () =>
      
      val userID = for {
        state <- S.param("state") if OAuth2State.get.exists(_ == state)
        code <- S.param("code")
        userID <- getUserID(getService, code, "https://graph.facebook.com/me?fields=id,name")
      } yield {
        "fb" + userID
      }

      println("userID:" + userID)

      userID match {
        case Full(userID) => Full(OkResponse())
        case _ => Full(RedirectWithState("/login/", RedirectState(() => S.notice("登入錯誤，請重試"))))
      }
    }

  }

}

class Boot 
{

  lazy val siteMap = SiteMap(
    Menu("Home") / "index",
    Menu("Shop") / "store" / *,
    Menu("login") / "login" / *,
    Menu("login shop fb") / "login" / "shop" / "facebook" >> Login.Facebook.redirect,
    Menu("login shop fb") / "login" / "shop" / "google" >> Login.Google.redirect,
    Menu("auth shop fb") / "login" / "shop" / "facebook" / "auth" >> Login.Facebook.auth,
    Menu("auth shop fb") / "login" / "shop" / "google" / "auth" >> Login.Google.auth

  )

  def boot 
  {
    LiftRules.addToPackages("org.bone.nongmo")

    LiftRules.htmlProperties.default.set { r: Req => 
      new Html5Properties(r.userAgent)
    }

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd )

    LiftRules.setSiteMap(siteMap)
  }
}

