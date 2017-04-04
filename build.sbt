import de.heikoseeberger.sbtheader.HeaderPattern
import sbt.Keys._
import sbt._

autoScalaLibrary := false

val copyright = headers := Map(
  "scala" -> (
    HeaderPattern.cStyleBlockComment,
    """|/*
       | * Copyright (c) 2016 Tinkoff
       | *
       | * Licensed under the Apache License, Version 2.0 (the "License");
       | * you may not use this file except in compliance with the License.
       | * You may obtain a copy of the License at
       | *
       | *    http://www.apache.org/licenses/LICENSE-2.0
       | *
       | * Unless required by applicable law or agreed to in writing, software
       | * distributed under the License is distributed on an "AS IS" BASIS,
       | * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       | * See the License for the specific language governing permissions and
       | * limitations under the License.
       | */
       |
       | """.stripMargin
  )
)

val setts = Seq(organization := "ru.tinkoff",
  version := "1.1.13",
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  copyright,
  licenses := Seq(("Apache License, Version 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))),
  homepage := Some(url("http://tinkoff.ru")),
  sonatypeProfileName := "ru.tinkoff",
  pgpReadOnly := false,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra in Global := {
    <developers>
      <developer>
        <id>DanyMariaLee</id>
        <name>Marina Sigaeva</name>
        <url>http://twitter.com/besseifunction</url>
      </developer>
    </developers>
  },
  scmInfo := Some(ScmInfo(
    url("http://github.com/TinkoffCreditSystems"),
    "scm:git:github.com/TinkoffCreditSystems/aerospike-scala",
    Some("scm:git:git@github.com:TinkoffCreditSystems/aerospike-scala.git")
  )),
  credentials += Credentials("Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    "",
    ""))

lazy val protoSetting = PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

val testLibs = Seq(
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.2.26" % "test",
  "junit" % "junit" % "4.12",
  "com.github.danymarialee" %% "aerospike-mock" % "1.0.4"
)

val mainLib = Seq(
  "com.aerospike" % "aerospike-client" % "3.3.1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "org.scala-lang" % "scala-reflect" % "2.11.8")

lazy val macros = Project(id = "aerospike-scala-macros",
  base = file("aerospike-scala-macros"), dependencies = Seq(domain))
  .settings(setts ++ (libraryDependencies ++= mainLib))

lazy val domain = Project(id = "aerospike-scala-domain",
  base = file("aerospike-scala-domain"))
  .settings(setts ++ (moduleName := "aerospike-scala-domain"))

lazy val protoBin = Project(id = "aerospike-scala-proto",
  base = file("aerospike-scala-proto"), dependencies = Seq(root))
  .settings(setts ++ (moduleName := "aerospike-scala-proto") ++
    (libraryDependencies ++= mainLib ++ testLibs) ++ protoSetting)

lazy val example = Project(id = "aerospike-scala-example",
  base = file("aerospike-scala-example"), dependencies = Seq(root, protoBin))
  .settings(setts ++ (moduleName := "aerospike-scala-example") ++
    (libraryDependencies ++= mainLib) ++ protoSetting)


lazy val root = Project(id = "aerospike-scala",
  base = file("."),
  dependencies = Seq(domain, macros))
  .settings(setts,
    libraryDependencies ++= mainLib ++ testLibs)