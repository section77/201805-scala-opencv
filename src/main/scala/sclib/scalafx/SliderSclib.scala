package sclib.scalafx

import scalafx.scene.control.Slider
import scalafx.Includes._

class SliderSclib(range: Range, cb: Number => Unit) extends Slider {
  min = range.min
  value = range.min
  max = range.max
  blockIncrement = range.min
  showTickLabels = true
  majorTickUnit = range.min

  this.valueProperty.onChange[Number]((ov: Any, oldV: Number, newV: Number) => {
    cb(newV)
  })
}
