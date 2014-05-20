import sbt._

import Keys._
import AndroidKeys._

object AndroidBuild extends Build {

    val baseSettings = Defaults.defaultSettings ++ Seq (
        name := "MaidroidReminderFree",
        version := "1.0.1",
        scalaVersion := "2.8.2",
        versionCode in Android := 2,
        platformName in Android := "android-13",
        scalacOptions += "-deprecation"
    )

    def unmanagedJars = (file(".") / "lib") ** "*.jar"

    lazy val fullAndroidSettings =
        AndroidProject.androidSettings ++
        TypedResources.settings ++
        AndroidMarketPublish.settings ++ Seq (
            keyalias in Android := "BrianHsu",
            proguardOption in Android += " -keep class scala.collection.generic.CanBuildFrom",
            proguardOption in Android += " -keep class scala.collection.Seq",
            proguardOption in Android += " -keep class scala.collection.Iterable",
            proguardOption in Android += " -keep class scala.collection.TraversableOnce",
            proguardOption in Android += " -keep class scala.collection.mutable.StringBuilder",
            proguardOption in Android += " -keep class scala.*",
            proguardOption in Android += " -keep class scala.math.*",
            proguardOption in Android += " -keep class scala.reflect.*",
            proguardInJars in Android ++= unmanagedJars.get,
            libraryDependencies += "org.scalatest" % "scalatest_2.8.1" % "1.5.1"
        )

    lazy val main = Project (
        "MaidroidReminderFree",
        file("."),
        settings = baseSettings ++ fullAndroidSettings ++ AndroidManifestGenerator.settings
    )

}
