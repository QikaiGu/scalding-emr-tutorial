// Required when using sbt-assembly
import AssemblyKeys._

organization := "com.sharethrough"

name         := "emr_tutorial"

version      := "1.0"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "cascading-repo" at "http://conjars.org/repo/"
)

// As of 2013-09-26, Hadoop 1.0.3 is the latest supported at Amazon, so we'll
// want to build against that.
libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-core" % "1.0.3" % "provided",
  "com.twitter" %% "scalding-core" % "0.8.11",
  "com.twitter" %% "scalding-date" % "0.8.11",
  "com.twitter" %% "scalding-args" % "0.8.11"
)

// Required for sbt-assembly
assemblySettings

mainClass in assembly := Some("com.sharethrough.emr_tutorial.JobRunner")

// Borrowed via twitter/scalding, used to resolve the conflicts that occur when
// creating a fat jar.
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case s if s.endsWith(".class") => MergeStrategy.last
    case s if s.endsWith("project.clj") => MergeStrategy.concat
    case s if s.endsWith(".html") => MergeStrategy.last
    case s if s.endsWith(".dtd") => MergeStrategy.last
    case s if s.endsWith(".xsd") => MergeStrategy.last
    case x => old(x)
  }
}
