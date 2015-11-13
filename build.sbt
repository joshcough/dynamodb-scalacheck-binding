name    := "dynamodb-scalacheck-binding"

version := "1.0-SNAPSHOT"

// Like a Boss.
initialCommands in console := "import scalaz._, Scalaz._"

libraryDependencies ++= Seq(
   "com.amazonaws"   % "aws-java-sdk-dynamodb"     % "latest.release"
  ,"com.amazonaws"   % "aws-lambda-java-events"    % "1.1.0"
  ,"org.scalaz"     %% "scalaz-core"               % "7.1.5"
  ,"org.scalacheck" %% "scalacheck"                % "1.12.5"
  ,"org.scalaz"     %% "scalaz-scalacheck-binding" % "7.1.5"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture")

resolvers += "Localytics Artifactory" at "http://localytics.artifactoryonline.com/localytics/maven-local"

val afPublishRepo = Some("localytics-release" at "http://localytics.artifactoryonline.com/localytics/maven-local")

credentials += Credentials(Path.userHome / ".sbt" / "credentials" / "localytics_artifactory.props")

publishTo := afPublishRepo

releaseSettings