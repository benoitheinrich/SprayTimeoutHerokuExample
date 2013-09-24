import sbt._
import Keys._

object Plugins extends Build {
  lazy val plugins = Project(id = "plugins", base = file("."),
    settings = Defaults.defaultSettings ++ Seq(
      addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.9.0")
    )
  )
}
