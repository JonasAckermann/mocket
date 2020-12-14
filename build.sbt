lazy val mocket =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings( // Normal SBT settings
      name := "mocket",
      version := "0.0.1",
      scalaVersion := "2.13.3",
      organization := "mocket",
      libraryDependencies ++= Seq(
        "com.lihaoyi"    %%% "utest"      % "0.7.4"  % "test",
        "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
      ),
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
    .settings( // Indigo specific settings
      showCursor := true,
      title := "Mocket - Made with Indigo",
      gameAssetsDirectory := "assets",
      windowStartWidth := 550,
      windowStartHeight := 400,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % "0.5.0",
        "io.indigoengine" %%% "indigo"            % "0.5.0",
        "io.indigoengine" %%% "indigo-extras"     % "0.5.0"
      )
    )

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")
addCommandAlias("runGame", ";compile;fastOptJS;indigoRun")
