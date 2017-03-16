name := "scrawla"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5-SNAPSHOT",
  "com.typesafe.akka" %% "akka-cluster" % "2.5-SNAPSHOT",
  "com.typesafe.akka" %% "akka-persistence" % "2.5-SNAPSHOT",
  "org.jsoup" % "jsoup" % "1.9.1",
  "org.apache.spark" %% "spark-sql" % "2.1.0",
  "joda-time" % "joda-time" % "2.9.7")
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.4"
