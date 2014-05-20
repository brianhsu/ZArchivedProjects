import java.io.File
import scala.xml.XML
import net.liftweb.util._
import net.liftweb.common._

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class WebAppTest extends FlatSpec with ShouldMatchers
{

    "A Web App" should "have well-formed XML files" in {

        val invalidXML = this.invalidXmlList()

        assert (invalidXmlList == Nil, 
                "Malformed XML in the following file:\n" + 
                invalidXmlList.mkString("  ===> ", "\n  ===> ", "\n  ===> Stack Trace:" ))
    }

    
    /**
     * Tests to make sure the project's XML files are well-formed.
     *
     * Finds every *.html and *.xml file in src/main/webapp (and its
     * subdirectories) and tests to make sure they are well-formed.
     */
    def invalidXmlList() = {
        var failed: List[File] = Nil

        def handledXml  (file: String) = file.endsWith(".xml")
        def handledXHtml(file: String) = file.endsWith(".html") || file.endsWith(".htm") || file.endsWith(".xhtml")

        def wellFormed(file: File) {
            if (file.isDirectory)
                for (f <- file.listFiles) wellFormed(f)

            if (file.isFile && handledXml(file.getName)) {
                try {
                    XML.loadFile(file)
                } catch {
                    case e: org.xml.sax.SAXParseException => failed ::= file
                }
            }

            if (file.isFile && handledXHtml(file.getName)) {
                PCDataXmlParser(new _root_.java.io.FileInputStream(file.getAbsolutePath)) match {
                    case Full(_) => // file is ok
                    case e       => failed ::= file ; println ("Error:"+e)
                }
            }
        }

        wellFormed(new File("src/main/webapp"))
        failed
    }
}
