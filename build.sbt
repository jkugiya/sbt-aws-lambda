import scala.sys.process._

scalaVersion := "2.12.8"

name := "sbt-aws-lambda"

organization := "com.gilt.sbt"

enablePlugins(SbtPlugin)

libraryDependencies += {
  val sbtV     = (sbtBinaryVersion in pluginCrossBuild).value
  val scalaV   = (scalaBinaryVersion in update).value
  val assembly = "com.eed3si9n" % "sbt-assembly" % "0.14.9"
  Defaults.sbtPluginExtra(assembly, sbtV, scalaV)
}

val awsSdkVersion = "2.9.20"

libraryDependencies ++= Seq(
  "software.amazon.awssdk"  % "iam"    % awsSdkVersion,
  "software.amazon.awssdk"  % "lambda" % awsSdkVersion,
  "software.amazon.awssdk"  % "s3"     % awsSdkVersion,
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
)

javaVersionPrefix in javaVersionCheck := Some("1.8")

crossSbtVersions := List("0.13.18", "1.2.8")

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

// Testing
libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.6" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")
