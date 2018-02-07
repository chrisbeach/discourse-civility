name := "discourse-civility"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akkaVersion = "2.5.9"
  val akkaHttpVersion = "10.0.11"

  Seq(
    "org.postgresql" % "postgresql" % "42.1.4",
    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0-RC1",
    "org.tpolecat" %% "doobie-core" % "0.4.4",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )
}
