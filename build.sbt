val openTelemetryVersion = "0.17.1"
val openTelemetryInstrumentationVersion = "1.0.0-alpha"
val akkaHttpVersion = "10.2.4"
val munitVersion = "0.7.22"

lazy val root = Project("root", file("."))
  .aggregate(core, akkaHttp, stdFuture, testkit, catsEffect2, catsEffect3)
  .settings(commonSettings)

lazy val core = Project("core", file("modules/scotel-core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-api" % openTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % openTelemetryVersion,
    ),
  )

lazy val testkit = Project("scotel-testkit", file("modules/scotel-testkit"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % munitVersion,
    ),
  )

lazy val stdFuture =
  Project("scotel-std-future", file("modules/scotel-std-future"))
    .settings(commonSettings)
    .dependsOn(core, testkit % testOnly)

lazy val catsEffect2 =
  Project("scotel-cats-effect-2", file("modules/scotel-cats-effect-2"))
    .dependsOn(core, testkit % testOnly)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-effect" % "2.3.3",
      ),
    )

lazy val catsEffect3 =
  Project("scotel-cats-effect-3", file("modules/scotel-cats-effect-3"))
    .dependsOn(core, testkit % testOnly)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-effect" % "3.0.0-RC2",
      ),
    )

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
