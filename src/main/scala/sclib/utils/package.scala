package sclib

import java.io._

package object utils {
  type Path = String

  def pathOfResource(name: String): Either[String, Path] =
    Option(getClass.getResource(name))
      .fold[Either[String, String]](Left(resourceNotFound(name)))(r => Right(r.getPath))

  def unsafePathOfResource(name: String): Path =
    pathOfResource(name).getOrElse(throw new Exception(resourceNotFound(name)))

  def resource(name: String): Either[String, BufferedInputStream] = {
    Option(getClass.getResourceAsStream(name))
      .map(new BufferedInputStream(_))
      .fold[Either[String, BufferedInputStream]](Left(resourceNotFound(name)))(Right.apply)
  }

  def extractResource(name: String): Either[String, Path] = {
    resource(name).map { resource =>
      val fh = File.createTempFile(name, ".tmp")

      val fos = new FileOutputStream(fh)
      Iterator.continually(resource.read()).takeWhile(-1 !=).foreach(fos.write)
      fos.close()

      fh.getAbsolutePath

    }
  }

  def unsafeExtractResource(name: String): Path =
    extractResource(name).getOrElse(throw new Exception(resourceNotFound(name)))

  private val resourceNotFound: String => String = name => s"resource: '${name}' not found"

}
