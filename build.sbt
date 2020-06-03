name := "cstar-audit-nats-trigger"
organization := "io.sfpl"
version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies ++= {

  Seq(
    "com.typesafe.akka"         %% "akka-actor"       % "2.4.14",
    "com.typesafe.akka"         %% "akka-slf4j"       % "2.4.14",
    "org.apache.cassandra"      % "cassandra-all"     % "3.11.0",
    "com.github.tyagihas"       % "scala_nats_2.11"   % "0.3.0",
    "net.liftweb"               %% "lift-json"        % "3.4+",
    "org.scalatest"             % "scalatest_2.11"    % "2.2.1"               % "test"
  )
}

//unmanagedJars in Compile += file("./lib/jnats-2.6.8.jar")
//libraryDependencies ++= Seq("io.nats" % "client" % "2.6.8" from "file://./lib/jnats-2.6.8.jar")
//javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8", "-g:lines")
crossPaths := false
autoScalaLibrary := false

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

javaHome.in(Compile) := {
  Some(file(sys.props("java.home")).getParentFile)
}

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "module-info.class" => MergeStrategy.discard
  case x => MergeStrategy.first
}
