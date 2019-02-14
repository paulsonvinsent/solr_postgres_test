name := "solr-postgre-load-test"

organization := "com.hortonworks.dss"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.apache.solr" % "solr-solrj" % "7.7.0",
  "com.github.finagle" %% "roc-core" % "0.0.4",
  "com.github.finagle" %% "roc-types" % "0.0.4"
)
