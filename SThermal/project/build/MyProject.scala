import sbt._

class MyProject(info: ProjectInfo) extends DefaultProject(info) with ProguardProject
{
    override def mainClass: Option[String] = Some("org.bone.SThermal")

    //proguard
    override def proguardOptions = List(
      "-keepclasseswithmembers public class * { public static void main(java.lang.String[]); }",
      "-dontshrink",
      "-dontoptimize",
      "-dontobfuscate",
      proguardKeepLimitedSerializability,
      proguardKeepAllScala,
      "-keep interface scala.ScalaObject"
    )

    override def proguardInJars = Path.fromFile(scalaLibraryJar) +++ super.proguardInJars
}
