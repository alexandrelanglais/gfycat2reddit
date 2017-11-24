import wartremover.WartRemover.autoImport.Wart

name := "Gfycat 2 Reddit"

organization := "io.trailermaker"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.4"

isSnapshot := true

resolvers += Resolver.bintrayRepo("freshwood", "maven")
resolvers += Resolver.jcenterRepo

libraryDependencies += "net.softler"                %% "akka-http-rest-client" % "0.1.0"
libraryDependencies += "com.typesafe.akka"          %% "akka-http-spray-json"  % "10.0.10"
libraryDependencies += "com.typesafe.akka"          % "akka-testkit_2.12"      % "2.5.6" % "test"
libraryDependencies += "com.typesafe.akka"          % "akka-testkit_2.12"      % "2.5.6" % "test"
libraryDependencies += "ch.qos.logback"             % "logback-classic"        % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"         % "3.7.2"
libraryDependencies += "org.scalactic"              %% "scalactic"             % "3.0.4"
libraryDependencies += "org.scalatest"              %% "scalatest"             % "3.0.4" % "test"
libraryDependencies += "com.github.pathikrit"       %% "better-files"          % "3.2.0"
libraryDependencies += "org.reactivemongo"          %% "reactivemongo"         % "0.12.6"
libraryDependencies += "net.dean.jraw"              % "JRAW"                   % "0.9.0"

wartremoverErrors ++= Warts.allBut(
  Wart.DefaultArguments,
  Wart.Nothing,
  Wart.Equals,
  Wart.NonUnitStatements,
  Wart.Any,
  Wart.PublicInference,
  Wart.OptionPartial,
    Wart.ToString
)

mainClass in assembly := Some("io.trailermaker.gfycat2reddit.Gfycat2Reddit")
