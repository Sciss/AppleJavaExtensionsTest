name := "AppleJavaExtensionsTest"

organization := "de.sciss"

scalaVersion := "2.10.1"

libraryDependencies <+= scalaVersion { sv => "org.scala-lang" % "scala-swing" % sv }

retrieveManaged := true

// ---- packaging ----

seq(appbundle.settings: _*)

appbundle.target <<= baseDirectory

appbundle.documents += appbundle.Document(
  name       = "Text Document",
  role       = appbundle.Document.Viewer,
  extensions = Seq("*"),
  osTypes    = Seq("TEXT", "****")
)
