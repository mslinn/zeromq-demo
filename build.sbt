name := "ZeroMQ Demo"

version := "0.1"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation")

resolvers ++= Seq(
  //"Typesafe Snapshots"    at "http://repo.typesafe.com/typesafe/snapshots",
  "Typesafe Releases"     at "http://repo.typesafe.com/typesafe/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %  "akka-actor"                 % "2.0.1" withSources(),
  "com.typesafe.akka" %  "akka-remote"                % "2.0.1" withSources(),
  "com.typesafe.akka" %  "akka-kernel"                % "2.0.1" withSources(),
  "com.typesafe.akka" %  "akka-slf4j"                 % "2.0.1" withSources(),
  "org.zeromq"        %  "zeromq-scala-binding_2.9.1" % "0.0.5" withSources(),
  "com.typesafe.akka" %  "akka-zeromq"                % "2.0.1" withSources(),
  "com.typesafe.akka" %  "akka-testkit"               % "2.0.1" withSources(),
  "ch.qos.logback"    %  "logback-classic"            % "1.0.0"
)