name := "sclib/cats/xopencv"

scalaVersion := "2.12.6"


//val opencvPath = "lib"
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

unmanagedJars in Compile += file(s"${opencvPath}/opencv-2413.jar")

scalacOptions += "-feature"
scalacOptions += "-language:implicitConversions"
