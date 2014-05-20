name := "Maidroid Calendar"

version := "0.0.2"

scalaVersion := "2.8.2"


libraryDependencies ++= Seq (
    "org.xerial" % "sqlite-jdbc" % "3.6.16",
    "com.google.api.client" % "google-api-client" % "1.1.1-alpha",
    "org.scalatest" % "scalatest_2.8.1" % "1.5.1"
)

parallelExecution in Test := false

parallelExecution in IntegrationTest := false

