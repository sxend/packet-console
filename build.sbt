organization := "arimitsu.sf"

name := "packet-console"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.6"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
