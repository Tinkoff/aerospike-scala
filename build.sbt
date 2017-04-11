import de.heikoseeberger.sbtheader.HeaderPattern
import Dependencies._

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

val setts = Seq(
  organization := "ru.tinkoff",
  version := "1.1.14",
  scalaVersion := Versions.scala,
  crossScalaVersions := Versions.scalas,
  // Doge
  releaseCrossBuild := false,
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  copyright,
  licenses := Seq(
    ("Apache License, Version 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  ),
  homepage := Some(url("http://tinkoff.ru")),
  sonatypeProfileName := "ru.tinkoff",
  pgpReadOnly := false,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
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
  scmInfo := Some(
    ScmInfo(
      url("http://github.com/TinkoffCreditSystems"),
      "scm:git:github.com/TinkoffCreditSystems/aerospike-scala",
      Some("scm:git:git@github.com:TinkoffCreditSystems/aerospike-scala.git")
    )
  ),
  credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "", "")
)

lazy val protoSetting = PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

lazy val macros =
  Project(id = "aerospike-scala-macros", base = file("aerospike-scala-macros"), dependencies = Seq(domain))
    .settings(setts)
    .settings(libraryDependencies ++= mainLibs(scalaVersion.value))

lazy val domain = Project(id = "aerospike-scala-domain", base = file("aerospike-scala-domain"))
  .settings(setts)
  .settings(moduleName := "aerospike-scala-domain")

lazy val protoBin =
  Project(id = "aerospike-scala-proto", base = file("aerospike-scala-proto"), dependencies = Seq(root))
    .settings(setts)
    .settings(protoSetting)
    .settings(
      Seq(
        moduleName := "aerospike-scala-proto",
        libraryDependencies ++= commonLibs(scalaVersion.value)
      )
    )

lazy val example =
  Project(id = "aerospike-scala-example", base = file("aerospike-scala-example"), dependencies = Seq(root, protoBin))
    .settings(setts)
    .settings(protoSetting)
    .settings(
      Seq(
        moduleName := "aerospike-scala-example",
        libraryDependencies ++= exampleLibs(scalaVersion.value)
      )
    )

lazy val root =
  Project(id = "aerospike-scala", base = file("."), dependencies = Seq(domain, macros))
    .settings(setts)
    .settings(libraryDependencies ++= commonLibs(scalaVersion.value))

/**
  * Helpers
  */
lazy val cleanAll = taskKey[Unit](s"Clean all subprojects")
cleanAll in ThisBuild := clean
  .all(ScopeFilter(inAnyProject))
  .value
  .foreach(identity)

lazy val compileLibraries = taskKey[Unit](s"compile all libraries")
compileLibraries in ThisBuild := Def
  .sequential(
    compile in (macros, Compile),
    compile in (domain, Compile),
    compile in (root, Compile),
    compile in (protoBin, Compile)
  )
  .value

lazy val compileAll = taskKey[Unit](s"compile all subprojects")
compileAll in ThisBuild := Def
  .sequential(
    compileLibraries,
    compile in (example, Compile)
  )
  .value

lazy val recompileAll = taskKey[Unit](s"clean and compile all subprojects")
recompileAll in ThisBuild := Def
  .sequential(
    cleanAll,
    compileLibraries,
    compile in (example, Compile)
  )
  .value

lazy val compileAndTestAll = taskKey[Unit](s"compile all subprojects and test ${root.id}")
compileAndTestAll in ThisBuild := Def
  .sequential(
    compileAll,
    test in (root, Test)
  )
  .value

lazy val recompileAndTestAll = taskKey[Unit](s"clean, compile all subprojects and test ${root.id}")
recompileAndTestAll in ThisBuild := Def
  .sequential(
    recompileAll,
    test in (root, Test)
  )
  .value

lazy val publishLibrariesLocal = taskKey[Unit](s"publish all libraries locally")
publishLibrariesLocal in ThisBuild := Def
  .sequential(
    publishLocal in macros,
    publishLocal in domain,
    publishLocal in root,
    publishLocal in protoBin
  )
  .value

lazy val publishLibraries = taskKey[Unit](s"publish all libraries")
publishLibraries in ThisBuild := Def
  .sequential(
    publish in macros,
    publish in domain,
    publish in root,
    publish in protoBin
  )
  .value

import PgpKeys.publishSigned
lazy val publishSignedLibraries = taskKey[Unit](s"publishSigned all libraries")
publishSignedLibraries in ThisBuild := Def
  .sequential(
    publishSigned in macros,
    publishSigned in domain,
    publishSigned in root,
    publishSigned in protoBin
  )
  .value

lazy val publishSignedAll = taskKey[Unit](s"publishSigned all subprojects")
publishSignedAll in ThisBuild := Def
  .sequential(
    publishSignedLibraries,
    publishSigned in example
  )
  .value
