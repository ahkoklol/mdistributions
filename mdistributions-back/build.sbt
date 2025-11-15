val tapirVersion = "1.11.50"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "mdistributions-back",
    version := "0.1.0-SNAPSHOT",
    organization := "com.ahkoklol",
    scalaVersion := "3.7.3",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.18",
      "dev.zio" %% "zio-logging" % "2.1.15",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.15",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "dev.zio" %% "zio-test" % "2.0.13" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.13" % Test,
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.10.2" % Test,
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.postgresql" % "postgresql" % "42.7.3",
      "com.github.t3hnar" %% "scala-bcrypt" % "4.1",
      "com.sun.mail" % "jakarta.mail" % "2.0.1",
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)
