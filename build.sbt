name    := "dynamodb-scalacheck-binding"

version := "1.0-SNAPSHOT"

// Like a Boss.
initialCommands in console := "import scalaz._, Scalaz._"

libraryDependencies ++= Seq(
   // TODO: can this dep be changed to just dynamodb?
   "com.amazonaws"   % "aws-lambda-java-core"      % "1.1.0"
  ,"com.amazonaws"   % "aws-lambda-java-events"    % "1.1.0"
  ,"commons-io"      % "commons-io"                % "2.4"
  ,"org.scalaz"     %% "scalaz-core"               % "7.1.4"
  ,"org.scalacheck" %% "scalacheck"                % "1.12.5"
  ,"org.scalaz"     %% "scalaz-scalacheck-binding" % "7.1.4"
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