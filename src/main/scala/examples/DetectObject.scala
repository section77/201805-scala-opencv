package examples

import java.util

import scala.concurrent.duration._
import cats.effect._
import org.opencv.core._
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

import scala.concurrent.ExecutionContext.Implicits.global
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Label, Slider, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.layout._
import sclib._
import sclib.utils._
import sclib.cats.effect._
import sclib.opencv._

trait DetectObject {
  case class MinMax(min: Int, max: Int) {
    override def toString: String = s"$min -> $max"
  }
  implicit def tupleAsMinMax(t: Tuple2[Int, Int]): MinMax = MinMax(t._1, t._2)

  case class Thresholds(blur: Int,
                        hue: MinMax,
                        saturation: MinMax,
                        value: MinMax,
                        erode: Int,
                        dilate: Int) {
    override def toString: String =
      s"blur: $blur, hue: ${hue}, saturation: $saturation, value: $value, erode: $erode, dilate: $dilate"
  }

  def detectAndMarkObject(frame: Mat, thresholds: Thresholds): IO[(Mat, Mat)] = IO {
    import thresholds._

    // remove noise
    val blurredImg = new Mat()
    Imgproc.blur(frame, blurredImg, new Size(blur, blur))

    // convert to HSV
    val hsvImg = new Mat()
    Imgproc.cvtColor(blurredImg, hsvImg, Imgproc.COLOR_BGR2HSV)

    // apply threshold / filter / mask
    val filteredImg = new Mat()
    Core.inRange(hsvImg,
                 new Scalar(hue.min, saturation.min, value.min),
                 new Scalar(hue.max, saturation.max, value.max),
                 filteredImg)

    // morphism
    val morhismImg = new Mat()
    Imgproc.dilate(filteredImg,
                   morhismImg,
                   Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilate, dilate)))

    Imgproc.erode(morhismImg,
                  morhismImg,
                  Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erode, erode)))

    // find objects and mark them
    val contours = new util.ArrayList[MatOfPoint]

    val work = new Mat()
    morhismImg.copyTo(work)
    Imgproc.findContours(work, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE)

    for (n <- 0 until contours.size()) {
      Imgproc.drawContours(frame, contours, n, new Scalar(0, 0, 255), 3)

      val moments  = Imgproc.moments(contours.get(n))
      val centroid = new Point()
      centroid.x = moments.get_m10 / moments.get_m00
      centroid.y = moments.get_m01 / moments.get_m00

      Imgproc.circle(frame, centroid, 6, new Scalar(0, 0, 255))

    }

    (filteredImg, morhismImg)
  }
}

object DetectObjectApp extends JFXApp with DetectObject with OpenCVApp {
  case class ValueController(name: String, ui: Pane, minMax: () => MinMax)

  //
  // setup gui
  //
  val sldHue        = mkValueControllers("Hue", 0, 179)
  val sldSaturation = mkValueControllers("Saturation", 0, 255)
  val sldValue      = mkValueControllers("Value", 0, 255)
  val sldBlur       = new Slider { min = 1; value = 1 }
  val sldErode      = new Slider { min = 1; value = 1 }
  val sldDilate     = new Slider { min = 1; value = 1 }

  val frameImgView    = new ImageView { preserveRatio = true; fitWidth = 425 }
  val filteredImgView = new ImageView { preserveRatio = true; fitWidth = 200 }
  val morphImgView    = new ImageView { preserveRatio = true; fitWidth = 200 }
  val lblThresholds   = new Label()

  stage = new PrimaryStage {
    scene = new Scene {
      content = new BorderPane {
        padding = Insets(10)

        center = new HBox {
          padding = Insets(10)
          children = List(
            new FlowPane {
              children = List(mkLabelFor(frameImgView, "Frame"), frameImgView)
            },
            new VBox {
              children = List(
                mkLabelFor(filteredImgView, "Filtered (HSV thresholds)"),
                filteredImgView,
                mkLabelFor(morphImgView, "Erode / Dilate"),
                morphImgView
              )
            }
          )
        }

        bottom = new VBox {
          children = List(
            mkLabelFor(sldBlur, "Blur kernel size"),
            sldBlur,
            sldHue.ui,
            sldSaturation.ui,
            sldValue.ui,
            mkLabelFor(sldErode, "Erode"),
            sldErode,
            mkLabelFor(sldDilate, "Dilate"),
            sldDilate,
            lblThresholds
          )
        }
      }
    }
  }

  //
  // grab a frame from the webcam and mark all objects on it
  //
  val cam = WebCam(deviceId = 1)

  val detectObject = for {
    //frame <- IO(Imgcodecs.imread(unsafeExtractResource("/circles.png")))
    frame <- IO(Imgcodecs.imread(unsafeExtractResource("/tennis-ball.jpg")))

    //frame <- cam.grabFrame()
    thresholds = Thresholds(sldBlur.getValue.toInt,
                            sldHue.minMax(),
                            sldSaturation.minMax(),
                            sldValue.minMax(),
                            sldErode.getValue.toInt,
                            sldDilate.getValue.toInt)
    res <- detectAndMarkObject(frame, thresholds)
  } yield
    Platform.runLater {
      lblThresholds.text = thresholds.toString
      frameImgView.setImage(frame.toFXImage)
      filteredImgView.setImage(res._1.toFXImage)
      morphImgView.setImage(res._2.toFXImage)
    }

  repeatWithFixedDelay(200.millis, detectObject).unsafeRunAsync(_.fold(_.printStackTrace, println))

  //
  // gui helper
  //
  def mkValueControllers(name: String, _min: Int, _max: Int): ValueController = {

    class MySlider(_value: Int, tip: String) extends Slider {
      min = _min
      max = _max
      value = _value
      tooltip = new Tooltip(tip)
      blockIncrement = 1
      minWidth = 266
    }
    val sldMin = new MySlider(_min, "minimum threshold")
    val sldMax = new MySlider(_max, "maximum threshold")

    val ui = new FlowPane {
      padding = Insets(10)
      minWidth = 600
      children = new Label {
        text = name
        prefWidth = 90
      } :: sldMin :: sldMax :: Nil

    }
    ValueController(
      name,
      ui,
      () => sldMin.getValue.toInt -> sldMax.getValue.toInt
    )
  }

  def mkLabelFor(node: Node, t: String) = new Label {
    text = t
    labelFor = node
  }
}
