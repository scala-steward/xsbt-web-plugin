libraryDependencies += "dev.zio" %% "zio" % "2.0.0-M1"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "com.h2database" % "h2" % "1.4.200"
libraryDependencies += "com.jolbox" % "bonecp" % "0.8.0.RELEASE"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

enablePlugins(JettyPlugin)

containerForkOptions :=
  ForkOptions().withEnvVars {
    Map(
      "DB_DRIVER" -> "org.h2.Driver",
      "DB_URL" -> "jdbc:h2:mem:zio",
      "DB_USER" -> "sa",
      "DB_PASS" -> ""
    )
  }
