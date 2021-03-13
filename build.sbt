val openTelemetryVersion = "0.17.1"
val openTelemetryInstrumentationVersion = "1.0.0-alpha"
val akkaHttpVersion = "10.2.4"
val munitVersion = "0.7.22"

lazy val root = Project("root", file("."))
  .aggregate(core, akkaHttp, stdFuture)
  .settings(commonSettings)

lazy val core = Project("core", file("modules/scotel-core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-api" % openTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % openTelemetryVersion,
    ),
  )

lazy val stdFuture =
  Project("scotel-std-future", file("modules/scotel-std-future"))
    .settings(commonSettings)
    .dependsOn(core, testkit % testOnly)

lazy val testkit = Project("scotel-testkit", file("modules/scotel-testkit"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % munitVersion,
    ),
  )

//lazy val logbackCirce = Project("logback", file("modules/logback"))
//  .dependsOn(core)
//  .settings(commonSettings)
//  .settings(
//    libraryDependencies ++= Seq(
//      "ch.qos.logback" % "logback-classic" % "1.2.3",
//      "io.circe" %% "circe-core" % "0.13.0",
//    ),
//  )

// FIXME: rename
lazy val akkaHttp =
  Project("scotel-akka-http", file("modules/scotel-akka-http"))
    .dependsOn(core, testkit % testOnly)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream" % "2.6.13",
      ),
    )

lazy val commonSettings = Seq(
  version := "0.1.0",
  organization := "com.github.jatcwang",
  scalaVersion := "2.13.4",
  scalacOptions --= {
    if (sys.env.get("CI").isDefined) {
      Seq.empty
    } else {
      Seq("-Xfatal-warnings")
    }
  },
  libraryDependencies ++= Seq("org.scalameta" %% "munit" % munitVersion % Test),
  testFrameworks += new TestFramework("munit.Framework"),
  addCompilerPlugin(
    "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full,
  ),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
)

val testOnly = "test->compile;test->test"
