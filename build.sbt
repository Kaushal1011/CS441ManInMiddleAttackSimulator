ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.17"

lazy val root = (project in file("."))
  .settings(
    name := "CS441HW2MitM",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.4.1" ,
      "org.apache.spark" %% "spark-sql" % "3.4.1" ,
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "org.apache.spark" %% "spark-graphx" % "3.4.1",
      "com.typesafe" % "config" % "1.4.1",
    )
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

