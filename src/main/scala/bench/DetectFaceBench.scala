package bench

import cats.effect.IO
import examples.DetectFace
import org.opencv.highgui.Highgui
import org.scalameter._

import sclib._
import sclib.opencv._

object DetectFaceBench extends Bench.LocalTime with OpenCVApp with DetectFace {

  val detectFace = for {
    img           <- IO(Highgui.imread(unsafePathOfResource("/lena1.png")))
    numberOfFaces <- detectAndMarkFaces(img)
  } yield numberOfFaces

  config(
    Key.exec.benchRuns -> 5,
    Key.verbose        -> true
  ) withWarmer {
    new Warmer.Default
  } measure {
    detectFace.unsafeRunSync()
  }

}
