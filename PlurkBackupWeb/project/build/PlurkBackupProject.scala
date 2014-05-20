import sbt._

class PlurkBackupProject(info: ProjectInfo) extends DefaultWebProject(info)
{
    val scalaToolsSnapshots = ScalaToolsSnapshots
    val liftVersion = "2.2-M1"
  
    /**************************************************
     * Lift Framework
     *************************************************/
    val liftWebkit = "net.liftweb" % "lift-webkit_2.8.0"  % liftVersion
    // val liftMapper  = "net.liftweb" % "lift-mapper"  % liftVersion
    // val liftRecord  = "net.liftweb" % "lift-record"  % liftVersion
    // val liftWizard  = "net.liftweb" % "lift-wizard"  % liftVersion
    // val liftWidgets = "net.liftweb" % "lift-widgets" % liftVersion
    // val liftTestkit = "net.liftweb" % "lift-testkit" % liftVersion
  
    /**************************************************
     * 3rd Party Library
     *************************************************/
    //val database  = "com.h2database" % "h2"        % "1.2.137" % "runtime"
    //val scalaTest = "org.scalatest"  % "scalatest" % "1.2.1-SNAPSHOT"
    val httpClient = "commons-httpclient" % "commons-httpclient" % "3.1"
  
    /**************************************************
     * Jetty Server
     *************************************************/
    val jettyServer = "org.mortbay.jetty" % "jetty" % "6.1.24" % "test"
}
