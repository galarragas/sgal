import sbt.Keys._
import sbt._

object Testing {
  lazy val testAll = TaskKey[Unit]("test-all")

  private lazy val itSettings =
    inConfig(Configs.IntegrationTest)(Defaults.testSettings) ++
      Seq(
        fork in Configs.IntegrationTest := false,
        parallelExecution in Configs.IntegrationTest := false,
        scalaSource in Configs.IntegrationTest := baseDirectory.value / "src/it/scala")

  lazy val settings = itSettings ++ Seq(
    testAll <<= (test in Configs.IntegrationTest).dependsOn(test in Test)
  )
}