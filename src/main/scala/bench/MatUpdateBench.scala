package bench

import org.opencv.core._
import org.scalameter.api._
import sclib.opencv._

object MatUpdateBench extends Bench.LocalTime with OpenCVApp {

  private val range = Gen.range("size")(200, 1401, 400)

  performance of "Mat update" config (exec.jvmflags -> List("-Djava.library.path=./lib")) in {

    measure method "every cell" in {
      using(range) in { n =>
        val matrix = new Mat(n, n, CvType.CV_8U)
        for {
          x <- 0 until matrix.rows
          y <- 0 until matrix.cols
        } matrix.put(x, y, Array(100.toByte))
      }
    }

    measure method "batch" in {
      using(range) in { n =>
        val matrix = new Mat(n, n, CvType.CV_8U)
        val buffer = matrix.getBytes

        (0 until buffer.length).foreach { n =>
          buffer(n) = 100
        }

        matrix.put(0, 0, buffer)
      }
    }
  }

}
