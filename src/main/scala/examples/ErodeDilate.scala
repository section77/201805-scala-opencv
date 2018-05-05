import javafx.beans.value.{ChangeListener, ObservableValue}

import org.opencv.highgui.Highgui

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Label, Slider}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.VBox
import org.opencv.core.{Mat, Size}
import org.opencv.imgproc.Imgproc
import scalafx.geometry.Insets

import sclib._
import sclib.opencv._
import sclib.cats.effect._

object ErodeDilate extends JFXApp with OpenCVApp {

  val original  = Highgui.imread(unsafePathOfResource("/circles.png"))
  val imageView = new ImageView(original.toFXImage)

  class MySlider extends Slider {
    min = 1
    value = 1
    max = 20
    blockIncrement = 1
    showTickLabels = true
    showTickMarks = true
  }

  val sldErode = new MySlider
  sldErode.valueProperty.addListener(new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number],
                         oldVal: Number,
                         newVal: Number): Unit = {
      morph(newVal.intValue(), sldDilate.getValue.toInt)
    }
  })
  val sldDilate = new MySlider
  sldDilate.valueProperty.addListener(new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number],
                         oldVal: Number,
                         newVal: Number): Unit = {
      morph(sldErode.getValue.toInt, newVal.intValue())
    }
  })

  stage = new PrimaryStage {
    scene = new Scene {
      content = new VBox {
        spacing = 10
        padding = Insets(20)
        children = Seq(
          imageView,
          mkLabelFor(sldDilate, "Dilate"),
          sldDilate,
          mkLabelFor(sldErode, "Erode"),
          sldErode,
        )
      }
    }
  }

  def morph(erode: Int, dilate: Int): Unit = {
    imageView.setImage(morphImage(original, erode, dilate).toFXImage)
  }

  def morphImage(src: Mat, erode: Int, dilate: Int): Mat = {
    println("erode: ", erode, "dilate: ", dilate)
    val work = new Mat()
    Imgproc.dilate(src,
                   work,
                   Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilate, dilate)))
    Imgproc.erode(work,
                  work,
                  Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erode, erode)))
    return work
  }

  def mkLabelFor(node: Node, t: String) = new Label {
    text = t
    labelFor = node
  }
}
