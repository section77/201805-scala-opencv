import _root_.cats.syntax.either._

package object utils {

  def pathOfResource(name: String): Either[String, String] =
    Option(getClass.getResource(name))
      .fold(Either.left[String, String](s"resource: '${name}' not found"))(_.getPath.asRight)

  def unsafePathOfResource(name: String): String =
    pathOfResource(name).getOrElse(throw new Exception(s"resource: '${name}' not found"))
}
