package sclib.opencv

import cats.implicits._
import cats.effect._

import org.opencv.core.Mat
import org.opencv.highgui.VideoCapture

case class WebCam(val deviceId: Int) {
  private val capture = new VideoCapture(deviceId)

  def isConnected() = IO(capture.isOpened)

  def close() = IO(capture.release())

  def grabFrame(): IO[Mat] =
    IO(if (!capture.grab()) throw new Exception("no frame received")) *> IO {
      val frame = new Mat()
      capture.read(frame)
      frame
    }
}
