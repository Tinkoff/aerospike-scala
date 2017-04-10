import sbt._

//noinspection TypeAnnotation
object Dependencies {

  object Versions {
    val scala  = "2.12.1"
    val scalas = Seq("2.11.8", "2.12.1")

    val aerospikeClient = "3.3.1"
    val aerospikeMock   = "1.0.4"
    val akkaHttp        = "10.0.0"
    val junit           = "4.12"
    val mockito         = "2.2.26"
    val scalatest       = "3.0.1"
    val shapeless       = "2.3.2"
    val typesafeConfig  = "1.3.1"
  }

  val testLibs = Seq(
    "org.scalatest"           %% "scalatest"            % Versions.scalatest,
    "org.mockito"             % "mockito-core"          % Versions.mockito,
    "junit"                   % "junit"                 % Versions.junit,
    "com.typesafe.akka"       %% "akka-http-spray-json" % Versions.akkaHttp,
    "com.github.danymarialee" %% "aerospike-mock"       % Versions.aerospikeMock
  ).map(_ % "test")

  def mainLibs(scalaVersion: String) = Seq(
    "com.aerospike"  % "aerospike-client" % Versions.aerospikeClient,
    "com.chuusai"    %% "shapeless"       % Versions.shapeless,
    "org.scala-lang" % "scala-reflect"    % scalaVersion
  )

  def commonLibs(scalaVersion: String) = mainLibs(scalaVersion) ++ testLibs

  def exampleLibs(scalaVersion: String) =
    mainLibs(scalaVersion) ++
      Seq(
        "com.typesafe" % "config" % Versions.typesafeConfig
      )

}
