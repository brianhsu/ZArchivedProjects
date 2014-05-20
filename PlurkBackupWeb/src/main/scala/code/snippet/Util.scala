package code.snippet

import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds._

import scala.xml.NodeSeq

object Util
{
    def lazyLoad(xhtml: NodeSeq) = {
        val id = "lazy" + System.currentTimeMillis()
        val (name, javascript) = SHtml.ajaxInvoke(() => { SetHtml(id, xhtml) })

        <div id={id}>
            <img src="/images/ajax-loader.gif" height="32" width="32" alt="wait"/> 備份中，請稍候……
            {Script(OnLoad(javascript.cmd))}
        </div>
    }
}
