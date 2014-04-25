import play.Project._

name := """hello-play-scala"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.30",
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "org.webjars" %% "webjars-play" % "2.2.0", 
  "org.webjars" % "bootstrap" % "2.3.1"
)

playScalaSettings
