import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._


object MLlibTestsBuild extends Build {

  // Building the mllib tests is tricky. The easiset way is to set the mllib snapshots to the same
  // version and building like that. If you want to use an older build, i.e. a build that doesn't
  // have some of the new features, it's easiest to comment out the project with the recent
  // features from the root build. For example, in order to build with the Spark 1.0.0 release,
  // comment out .dependsOn(onepointone) from root.

  lazy val commonSettings = Seq(
    organization := "org.spark-project",
    version := "0.1",
    scalaVersion := "2.10.4",
    libraryDependencies ++= Seq(
      "net.sf.jopt-simple" % "jopt-simple" % "4.5",
      "org.scalatest" %% "scalatest" % "2.2.1" % "test",
      "org.slf4j" % "slf4j-log4j12" % "1.7.2"
    )
  )

  lazy val root = Project(
    "mllib-perf",
    file("."),
    settings = assemblySettings ++ commonSettings ++ Seq(
      test in assembly := {},
      outputPath in assembly := file("target/spark-perf-tests-assembly.jar"),
      assemblyOption in assembly ~= { _.copy(includeScala = false) },
      mergeStrategy in assembly := {
        case PathList("META-INF", xs@_*) =>
          (xs.map(_.toLowerCase)) match {
            case ("manifest.mf" :: Nil) => MergeStrategy.discard
            // Note(harvey): this to get Shark perf test assembly working.
            case ("license" :: _) => MergeStrategy.discard
            case ps@(x :: xs) if ps.last.endsWith(".sf") => MergeStrategy.discard
            case _ => MergeStrategy.first
          }
        case PathList("reference.conf", xs@_*) => MergeStrategy.concat
        case "log4j.properties" => MergeStrategy.discard
        case PathList("application.conf", xs@_*) => MergeStrategy.concat
        case _ => MergeStrategy.first
      }
    )).aggregate(onepointoh, onepointone).dependsOn(onepointoh, onepointone)

  lazy val onepointone = Project(
    "onepointone",
    file("onepointone"),
    settings = commonSettings ++ Seq(
      //should be set to 1.1.0 or higher
      libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.1.0" % "provided"
    )
  )

  lazy val onepointoh = Project(
    "onepointoh",
    file("onepointoh"),
    settings = commonSettings ++ Seq(
      //should be set to 1.0.0 or higher
      libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.0.0" % "provided"
    )
  )
}
