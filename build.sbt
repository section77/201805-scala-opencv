name := "opencv-examples"

scalaVersion := "2.12.7"


val opencvPath = {
  val os = System.getProperty("os.name")
  if (os.equals("FreeBSD")) "/usr/local/share/OpenCV/java"
  else "lib"
}

fork := true

libraryDependencies ++= Seq(
    "org.scalafx" %% "scalafx" % "8.0.144-R12"
  , "com.storm-enroute" %% "scalameter" % "0.9"
  , "org.typelevel" %% "cats-effect" % "1.0.0-RC"
)

unmanagedJars in Compile += file(s"${opencvPath}/opencv-343.jar")

javaOptions in run += s"-Djava.library.path=${opencvPath}"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-unused-import",
  "-Ywarn-numeric-widen",
  "-Ypartial-unification",
  "-Ybackend-parallelism", "4"
)



scalacOptions += "-language:implicitConversions"
