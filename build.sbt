import scala.sys.process._

scalaVersion := "2.12.10"

crossScalaVersions := Seq(scalaVersion.value, "2.13.1")

crossSbtVersions := List("1.2.8")

name := "sbt-aws-lambda"

organization := "com.gilt.sbt"

enablePlugins(SbtPlugin)

libraryDependencies += {
  val sbtV     = (sbtBinaryVersion in pluginCrossBuild).value
  val scalaV   = (scalaBinaryVersion in update).value
  val assembly = "com.eed3si9n" % "sbt-assembly" % "0.14.10"
  Defaults.sbtPluginExtra(assembly, sbtV, scalaV)
}

val awsSdkVersion = "2.10.19"

libraryDependencies ++= Seq(
  "software.amazon.awssdk"  % "iam"    % awsSdkVersion,
  "software.amazon.awssdk"  % "lambda" % awsSdkVersion,
  "software.amazon.awssdk"  % "s3"     % awsSdkVersion,
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
)

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

// Testing
libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.1" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")
