import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._

object Common {

  lazy val commonResolvers = Seq(
    DefaultMavenRepository,
    Resolver.typesafeRepo("releases"),
    "Conjars Repo" at "http://conjars.org/repo"
  )


  lazy val commonSettings: Seq[Setting[_]] = Seq (
    version := "0.1",
	  scalaVersion := "2.11.5",
    crossScalaVersions := Seq("2.11.5", "2.10.4"),
    organization := "uk.co.pragmasoft",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature"),
    resolvers ++= commonResolvers,
    test in assembly := {}
  ) ++
    net.virtualvoid.sbt.graph.Plugin.graphSettings

  implicit class PumpedProject(val project: Project) extends AnyVal {
    def testReportConfig =
      project
        .disablePlugins(plugins.JUnitXmlReportPlugin)
        .settings(
          (testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports", "-oD"),
          (testOptions in Test) += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console")
        )
  }

  val orientDbVersion = "2.0.1"
  val orientDbLuceneVersion = "2.0"

  val valiData = Seq( "uk.co.pragmasoft" %% "validata" % "0.1" )

  val orientDb = Seq(
    "com.orientechnologies" % "orientdb-core" % orientDbVersion excludeAll(ExclusionRule("org.jruby"), ExclusionRule("commons-beanutils")),
    "com.orientechnologies" % "orientdb-client" % orientDbVersion excludeAll(ExclusionRule("org.jruby"), ExclusionRule("commons-beanutils")),
    "com.orientechnologies" % "orientdb-object" % orientDbVersion excludeAll(ExclusionRule("org.jruby"), ExclusionRule("commons-beanutils")),
    "com.orientechnologies" % "orientdb-graphdb" % orientDbVersion excludeAll(ExclusionRule("org.jruby"), ExclusionRule("commons-beanutils")),
    "com.orientechnologies" % "orientdb-enterprise" % orientDbVersion excludeAll(ExclusionRule("org.jruby"), ExclusionRule("commons-beanutils")),
    "com.orientechnologies" % "orientdb-lucene" % orientDbLuceneVersion excludeAll(ExclusionRule("org.jruby"), ExclusionRule("commons-beanutils"))
  )

  val gremlinScala = Seq(
    ("com.michaelpollmeier" %% "gremlin-scala" % "2.6.1").excludeAll(ExclusionRule("commons-configuration"), ExclusionRule("commons-beanutils"))
  )
  

  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % "2.2.2" % "test"
  )

  
  val commonsCollections = Seq(
    "commons-collections" % "commons-collections" % "3.2"
  )

  
  val commonsLang = Seq("commons-lang" % "commons-lang" % "2.6")

}
