package sclib

import _root_.scalafx.scene.Node
import _root_.scalafx.scene.control.Label

package object scalafx {

  def mkLabelFor(node: Node, t: String) = new Label {
    text = t
    labelFor = node
  }
}
