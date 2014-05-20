package org.maidroid.calendar.model

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin
import com.google.api.client.googleapis.GoogleTransport
import com.google.api.client.googleapis.GoogleHeaders
import com.google.api.client.googleapis.GoogleUrl
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpExecuteIntercepter
import com.google.api.client.http.HttpResponseException

class MyRequest (val request: HttpRequest) 
{
    class SessionIntercepter(transport: HttpTransport, url: GoogleUrl) extends HttpExecuteIntercepter {
        var gsessionid: String = url.getFirst("gsessionid").asInstanceOf[String]
      
        transport.removeIntercepters(classOf[SessionIntercepter])
        transport.intercepters.add (0, this)
      
        def intercept(request: HttpRequest) {
            request.url.set("gsessionid", this.gsessionid);
        }
    }
       
    object RedirectHandler {
        
        def execute (request: HttpRequest): HttpResponse = try {
            request.execute()
        } catch {
            case e: HttpResponseException if e.response.statusCode == 302 =>
                val url = new GoogleUrl (e.response.headers.location)
                request.url = url
                new SessionIntercepter (request.transport, url)
                e.response.ignore
                request.execute()
            case e =>
                throw e
        }
    }

    def execute () = RedirectHandler.execute(request)
}

class GCalendarProtocol (username: String, password: String, applicationName: String)
{
    class MyTransport (val transport: HttpTransport) {
        def login(gDataType:String = "cl") {
            val login = new ClientLogin
            login.authTokenType = gDataType
            login.username = username
            login.password = password
            login.authenticate().setAuthorizationHeader (transport)
        }

        def buildRequest (url: String, ifMatch: Boolean = false)(requestBuilder: => HttpRequest) = {
            val request = requestBuilder
            request.setUrl(url)

            if (ifMatch) {
                request.headers.ifMatch = "*"
            }

            new MyRequest(request)
        }

        def buildGetRequest    (url: String) = buildRequest(url)(transport.buildGetRequest)
        def buildPostRequest   (url: String) = buildRequest(url)(transport.buildPostRequest)
        def buildDeleteRequest (url: String) = buildRequest(url, true)(transport.buildDeleteRequest)
        def buildPutRequest    (url: String) = buildRequest(url, true)(transport.buildPutRequest)
    }

    def createTransport (gdataVersion:String = "2"): MyTransport = {
        val transport = GoogleTransport.create()
        val headers = transport.defaultHeaders.asInstanceOf[GoogleHeaders]

        headers.setApplicationName (applicationName)
        headers.gdataVersion = gdataVersion

        new MyTransport(transport)
    }

    lazy val transport = createTransport ()

    def login() = transport.login()
}
