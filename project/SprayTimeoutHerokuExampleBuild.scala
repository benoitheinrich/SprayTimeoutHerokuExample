import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.example"
  val buildScalaVersion = "2.10.2"
  val buildVersion = "1.0.0-SNAPSHOT"

  val buildSettings = Defaults.defaultSettings ++
    Seq(
      organization := buildOrganization,
      scalaVersion := buildScalaVersion,
      version := buildVersion,
      resolvers := Resolvers.bootstrapResolvers,
      javacOptions ++= Seq("-Xlint:all", "-Werror"),
      scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xcheckinit", "-encoding", "utf8")
    )
}

object Resolvers {
  val bootstrapResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "spray repo" at "http://repo.spray.io",
    "spray repo - nightly" at "http://nightlies.spray.io"
  )
}

object Dependencies {
  val akkaVersion = "2.1.4"
  val logbackVersion = "1.0.13"
  val sprayVersion = "1.1-M8"
  val typeSafeConfigVersion = "1.0.2"

  val logback_classic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val logDeps = Seq(logback_classic)

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  val akkaDeps = Seq(akkaActor, akkaSlf4j, akkaTestKit)

  val typeSafeConfig = "com.typesafe" % "config" % typeSafeConfigVersion

  val spray = "io.spray" % "spray-can" % sprayVersion
  val sprayHttp = "io.spray" % "spray-http" % sprayVersion
  val sprayHttpx = "io.spray" % "spray-httpx" % sprayVersion
  val sprayRouting = "io.spray" % "spray-routing" % sprayVersion
  val sprayUtil = "io.spray" % "spray-util" % sprayVersion
  val sprayCaching = "io.spray" % "spray-caching" % sprayVersion
  val sprayTestskit = "io.spray" % "spray-testkit" % sprayVersion % "test"

  val sprayDeps = Seq(spray, sprayHttp, sprayHttpx, sprayRouting, sprayUtil, sprayCaching) ++ akkaDeps

  val sprayTestDeps = Seq(sprayTestskit)
}

object SprayTimeoutHerokuExampleBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import com.typesafe.sbt.SbtStartScript

  lazy val parent = Project(id = "SprayTimeoutHerokuExample", base = file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= (sprayDeps ++ sprayTestDeps ++ logDeps)
    ) ++ SbtStartScript.startScriptForClassesSettings
  )
}
