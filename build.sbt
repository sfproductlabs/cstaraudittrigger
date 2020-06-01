name := "cstar-audit-nats-trigger"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {

  Seq(
    "com.typesafe.akka"         %% "akka-actor"       % "2.4.14",
    "com.typesafe.akka"         %% "akka-slf4j"       % "2.4.14",
    "org.apache.cassandra"      % "cassandra-all"     % "3.11.0",
    "com.github.tyagihas"       % "scala_nats_2.11"   % "0.3.0"
    "org.scalatest"             % "scalatest_2.11"    % "2.2.1"               % "test"
  )
}

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "module-info.class" => MergeStrategy.discard
  case x => MergeStrategy.first
}
