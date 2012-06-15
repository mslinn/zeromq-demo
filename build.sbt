// see https://github.com/sbt/sbt-assembly
import AssemblyKeys._ // put this at the top of the file

name := "zeroMQBenchmark"

version := "0.2"

scalaVersion := "2.9.1-1"

scalacOptions ++= Seq("-deprecation")

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %  "akka-kernel"                % "2.0.1" withSources(),
  "com.typesafe.akka" %  "akka-zeromq"                % "2.0.1" withSources(),
  "ch.qos.logback"    %  "logback-classic"            % "1.0.0" withSources(),
  "org.zeromq"        %  "zeromq-scala-binding_2.9.1" % "0.0.6" withSources()
)

seq(assemblySettings: _*)
