import sbt._
import Process._

trait GNUGettext extends DefaultProject
{
    val potFile = sourcePath / "po" / "MaidroidGTDDesktop.pot"

    lazy val xgettext = task {
        val scalaSource = mainScalaSourcePath ** "*.scala"
        val javaSource = mainJavaSourcePath ** "*.java"
        val allSource = scalaSource.get ++ javaSource.get

        val command = <x> xgettext --from-code utf-8 -k_t -o {potFile} {allSource.mkString(" ")}</x>

        command ! log

        None
    }


    lazy val msgmerge = task {
        val targetDir = mainCompilePath
        val poDir: PathFinder = "src" / "po" * "*.po"

        for (poFile <- poDir.get) {
            val filename = poFile
            val msgmerge = <x> msgmerge -U {filename} {potFile} </x>
            log.info("Merge %s..." format(filename))
            msgmerge ! log
        }

        None
    }

    lazy val msgfmt = task { 
        val targetDir = mainCompilePath
        val poDir: PathFinder = "src" / "po" * "*.po"

        for (poFile <- poDir.get) {
            val filename = poFile
            val locale = filename.base
            val msgfmt = 
                <x> 
                    msgfmt --java -d {targetDir} 
                                  -r app.i18n.Messages
                                  -l {locale} {filename} 
                </x>

            log.info("Converting %s..." format(filename))
            msgfmt ! log
        }

        None
    } dependsOn(compile)
}

class MaidroidGTDProject(info: ProjectInfo) extends DefaultProject(info) 
                                            with assembly.AssemblyBuilder 
                                            with GNUGettext
{
    val javaMailRepo = "JavaMail" at "http://download.java.net/maven/2/"
    val getTextRepo = "gettext-commons-site" at "http://gettext-commons.googlecode.com/svn/maven-repository"

    val scalaQuery = "org.scalaquery" % "scalaquery_2.9.0" % "0.9.4"
    val sqlite = "org.xerial" % "sqlite-jdbc" % "3.6.16"
    val javaMail = "com.sun.mail" % "javax.mail" % "1.4.4-SNAPSHOT"
    val gettextCommons = "org.xnap.commons" % "gettext-commons" % "0.9.6"
    val jsoup = "org.jsoup" % "jsoup" % "1.6.0"

    //val scalaTest = "org.scalatest" % "scalatest" % "1.3"

    override def mainClass: Option[String] = Some("org.maidroid.gtd.desktop.GTDDesktop")

}
