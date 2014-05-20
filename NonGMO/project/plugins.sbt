// BeDone/project/plugins.scala

libraryDependencies += "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1"

resolvers += Resolver.url("sbt-plugin-releases 2",
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "less-sbt" % "0.1.10")
