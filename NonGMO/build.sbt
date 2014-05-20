name := "NonGMO"

version := "0.1"

scalaVersion := "2.10.0"

seq(webSettings :_*)

seq(lessSettings:_*)

(LessKeys.filter in (Compile, LessKeys.less)) := ("custom.less")

(resourceManaged in (Compile, LessKeys.less)) <<= 
    (sourceDirectory in Compile)(_ / "webapp" / "assets" / "bootstrap" / "custom")

(compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile)

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature", 
  "-language:postfixOps", "-language:implicitConversions"
)

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container",
  "org.scribe" % "scribe" % "1.3.5",
  "net.liftweb" %% "lift-webkit" % "2.5" % "compile->default"
)

port in container.Configuration := 8080
