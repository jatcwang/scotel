val openTelemetryVersion = "0.17.1"

lazy val root = Project("root", file("."))
  .aggregate(core, akkaHttp)
  .settings(commonSettings)

lazy val core = Project("core", file("modules/core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-api" % openTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % openTelemetryVersion,
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

lazy val akkaHttp =
  Project("scotel-akka-http", file("modules/scotel-akka-http"))
    .dependsOn(core)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % "10.2.4",
        "com.typesafe.akka" %% "akka-stream" % "2.6.13",
        "io.opentelemetry.javaagent.instrumentation" % "opentelemetry-javaagent-akka-http-10.0" % "0.17.0-alpha",
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
  addCompilerPlugin(
    "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full,
  ),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
)
