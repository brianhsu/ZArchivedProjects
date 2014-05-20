import sbt._

class MyProject(info: ProjectInfo) extends AndroidTask(info)
{
    def androidPath   = "/opt/android-sdk-update-manager"
    def androidVer    = "android-8"
    def scalaLibraryPath = "/opt/scala-2.8.0/lib/scala-library.jar"
}
