val tapirVersion = "1.11.50"
val zioVersion = "2.1.6"
val zioConfigVersion = "4.0.2"
val zioLoggingVersion = "2.3.0"
val doobieVersion = "1.0.0-RC5"
val jwtVersion = "10.0.0"
val zioCatsVersion = "23.1.0.2"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "mdistributions-back",
    version := "0.1.0-SNAPSHOT",
    organization := "com.ahkoklol",
    scalaVersion := "3.3.3", // Use a stable Scala 3 version

    // Add all missing dependencies
    libraryDependencies ++= Seq(
      // Tapir
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio"           % tapirVersion,

      // ZIO
      "dev.zio" %% "zio"               % zioVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      
      // ZIO Config
      "dev.zio" %% "zio-config"          % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,

      // Database (Doobie)
      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"   % doobieVersion,
      "dev.zio"      %% "zio-interop-cats_3" % zioCatsVersion, // For Doobie + ZIO

      // Security (JWT)
      "com.github.pjfanning" %% "pdi-jwt-zio-json" % jwtVersion,

      // Logging
      "ch.qos.logback" % "logback-classic" % "1.5.18",

      // Testing
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "dev.zio"                     %% "zio-test"               % zioVersion % Test,
      "dev.zio"                     %% "zio-test-sbt"           % zioVersion % Test,
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.9.7" % Test // Use a compatible version
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)