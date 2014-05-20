import sbt._

class MaidroidUtilsProject (info: ProjectInfo) extends DefaultProject (info)
{
    val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"
}
