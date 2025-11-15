val tapirVersion = "1.11.50"
val doobieVersion = "1.0.0-RC11" 
val zioConfigVersion = "4.0.5"
val zioTestVersion = "2.0.13"
val pdiJwtVersion = "2.1.2"
val zioInteropCatsVersion = "3.4.1"
val jbcryptVersion = "0.4" 

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "mdistributions-back",
    version := "0.1.0-SNAPSHOT",
    organization := "com.ahkoklol",
    scalaVersion := "3.7.3",
    libraryDependencies ++= Seq(
      // ... existing tapir/logging ...
      "dev.zio" %% "zio-logging" % "2.1.15",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.15",
      
      // --- Configuration ---
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,

      // --- Database & Interop ---
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.postgresql" % "postgresql" % "42.7.3",
      "dev.zio" %% "zio-interop-cats" % zioInteropCatsVersion, // FIX: Missing interop

      // --- Utilities (JWT & Hashing) ---
      "com.github.pjfanning" %% "pdi-jwt-zio-json" % pdiJwtVersion, // FIX: Missing JWT dependency
      "org.mindrot" % "jbcrypt" % jbcryptVersion, 
      "com.sun.mail" % "jakarta.mail" % "2.0.1",

      // ... existing test dependencies ...
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.10.2" % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)