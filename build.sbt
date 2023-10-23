ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "CS441HW2MitM",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.2.4" excludeAll(ExclusionRule(organization = "org.apache.hadoop")),
      "org.apache.spark" %% "spark-sql" % "3.2.4" excludeAll(ExclusionRule(organization = "org.apache.hadoop")),
      "org.apache.hadoop" % "hadoop-client" % "3.2.1",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test"

    )
  )
