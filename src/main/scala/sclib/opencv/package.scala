package sclib

import java.awt.image.{BufferedImage, DataBufferByte}

import org.opencv.core.{Core, Mat}

import scala.util.{Failure, Try}
import _root_.scalafx.embed.swing.SwingFXUtils
import _root_.scalafx.scene.image.WritableImage

package object opencv {

  trait OpenCVApp {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
  }

  implicit class MatOps(mat: Mat) {

    lazy val channels = mat.channels
    lazy val cols     = mat.cols
    lazy val rows     = mat.rows
    lazy val length   = channels * cols * rows

    def updated[A](f: Array[Byte] => A): Unit = {
      val buffer = getBytes
      f(buffer)
      setBytes(buffer)
    }

    def getBytes: Array[Byte] = {
      // allocate a buffer and store the `Mat` content in it
      val buffer = new Array[Byte](length)
      mat.get(0, 0, buffer)
      buffer
    }

    def setBytes(buffer: Array[Byte]): Try[Mat] = {
      if (buffer.size != length)
        Failure(new IllegalArgumentException(s"buffer size: ${buffer.size} != Mat size: ${length}"))
      else
        Try {
          mat.put(0, 0, buffer)
          mat
        }
    }

    def toBufferedImage: BufferedImage = {

      // create a empty `BufferedImage`
      import BufferedImage.{TYPE_3BYTE_BGR, TYPE_BYTE_GRAY}
      val imageType = if (channels > 1) TYPE_3BYTE_BGR else TYPE_BYTE_GRAY
      val image     = new BufferedImage(cols, rows, imageType)
      val imageData = image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData()

      // copy the buffer in the `BufferedImage`
      System.arraycopy(getBytes, 0, imageData, 0, length)

      image
    }

    def toFXImage: WritableImage = SwingFXUtils.toFXImage(toBufferedImage, null)
  }

}
