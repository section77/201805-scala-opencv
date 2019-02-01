package bench

import cats.effect.IO
import examples.DetectObject
import org.opencv.imgcodecs.Imgcodecs
import org.scalameter._
import sclib.utils._
import sclib.opencv._

object DetectObjectBench extends Bench.LocalTime with OpenCVApp with DetectObject {

  val detectObject = for {
    img <- IO(Imgcodecs.imread(unsafeExtractResource("/circles.png")))
    _   <- detectAndMarkObject(img, Thresholds(7, 0 -> 180, 0 -> 255, 117 -> 255, 12, 24))
  } yield ()

  config(
    Key.exec.benchRuns -> 5,
    Key.verbose        -> true
  ) withWarmer {
    new Warmer.Default
  } measure {
    detectObject.unsafeRunSync()
  }

}
