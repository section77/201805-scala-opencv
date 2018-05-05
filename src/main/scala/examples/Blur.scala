package examples

import javafx.beans.value.{ChangeListener, ObservableValue}

import org.opencv.highgui.Highgui
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.Slider
import scalafx.scene.image.ImageView
import scalafx.scene.layout.VBox
import org.opencv.core.{Mat, Size}
import org.opencv.imgproc.Imgproc

import sclib._
import sclib.opencv._

object Blur extends JFXApp with OpenCVApp {

  val original  = Highgui.imread(unsafePathOfResource("/lena1.png"))
  val imageView = new ImageView(original.toFXImage)

  val slider = new Slider {
    min = 1
    value = 1
    max = 50
    showTickLabels = true
    showTickMarks = true
  }

  //slider.onMouseMoved = (event) => println(event)
  slider.valueProperty.addListener(new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number],
                         oldVal: Number,
                         newVal: Number): Unit = {
      val work = new Mat()
      Imgproc.blur(original, work, new Size(newVal.intValue(), newVal.intValue()))
      imageView.setImage(work.toFXImage)
    }
  })

  stage = new PrimaryStage {
    scene = new Scene {
      content = new VBox {
        spacing = 10
        children = Seq(
          imageView,
          slider
        )
      }
    }
  }
}
