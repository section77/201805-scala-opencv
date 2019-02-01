package examples

import cats._
import cats.effect.IO
import cats.implicits._
import examples.DetectObjectApp.cam
import sclib.cats.effect._
import org.opencv.core._
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

import scala.concurrent.duration._
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.image.ImageView

import scala.concurrent.ExecutionContext.Implicits.global
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import sclib.utils._
import sclib.opencv._

trait DetectFace {

  def detectAndMarkFaces(img: Mat): IO[Int] =
    for {
      faceDetections <- detectFaces(img)
      _ <- faceDetections.traverse { rect =>
        val p1    = new Point(rect.x, rect.x)
        val p2    = new Point(rect.x + rect.width, rect.y + rect.height)
        val color = new Scalar(0, 255, 0)
        /*_*/
        IO(Imgproc.rectangle(img, p1, p2, color)) /*_*/
      }
    } yield faceDetections.length

  private val faceDetector = new CascadeClassifier(
    unsafePathOfResource("/lbpcascade_frontalface.xml"))

  private def detectFaces(img: Mat): IO[List[Rect]] = IO {
    val faceDetections = new MatOfRect()
    faceDetector.detectMultiScale(img, faceDetections)
    faceDetections.toArray.toList
  }
}

object DetectFaceApp extends JFXApp with DetectFace with OpenCVApp {

  //
  // setup gui
  //
  val imageView        = new ImageView()
  val lblNumberOfFaces = new Label()

  stage = new PrimaryStage {
    scene = new Scene {
      content = new VBox {
        spacing = 10
        children = Seq(
          lblNumberOfFaces,
          imageView
        )
      }
    }
  }

  //
  // grab a frame from the webcam and mark all faces on it
  //
  val cam = WebCam(deviceId = 1)

  val detectFace = for {
    frame <- IO(Imgcodecs.imread(unsafeExtractResource("/lena1.png")))
    //frame         <- cam.grabFrame()
    numberOfFaces <- detectAndMarkFaces(frame)
  } yield
    Platform.runLater {
      lblNumberOfFaces.text = s"Faces: ${numberOfFaces}"
      imageView.setImage(frame.toFXImage)
    }

  repeatWithFixedDelay(500.millis, detectFace).unsafeRunAsync(println)

}
