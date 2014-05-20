import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info)
{
    val android = "org.maidroid" % "sbt-android" % "1.0"
}
