import sbt.Keys._
import sbt._
import sbtbuildinfo.Plugin._
import scoverage.ScoverageSbtPlugin

object SgalBuild extends Build {
    import Common._

    val coreClasspath =
      gremlinScala ++
      scalaTest ++
      valiData

    val orientClasspath =
      orientDb ++
      scalaTest ++
      commonsLang

    val root =
      Project(id = "sgal", base = file("."))
        .settings( commonSettings: _* )
        .aggregate(core, orient)

  lazy val core =
    module("core")
      .settings(
        publishMavenStyle := true,
        libraryDependencies ++= coreClasspath
      )

  lazy val orient =
    module("orient")
      .settings(libraryDependencies ++= orientClasspath)
      .settings(
        publishMavenStyle := true
      )
      .settings(
        ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 70,
        ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true,
        fork := true
      )
      .settings(// Build info config
        buildInfoSettings: _*
      )
      .settings(
        javaOptions in Test ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")
      )
      .testReportConfig
      .dependsOn(core)

  def module(name: String) = {
    val id = "sgal-%s".format(name)
    Project(
      id = id,
      base = file(id),
      settings = commonSettings ++ Seq(Keys.name := id)
    )
  }


}


