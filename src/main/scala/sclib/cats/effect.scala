package sclib.cats

import cats.implicits._
import cats.effect.{IO, Timer}

import scala.concurrent.duration._

object effect {

  def repeatAtFixedRate(period: FiniteDuration, task: IO[Unit])(
      implicit timer: Timer[IO]): IO[Unit] = {

    for {
      start  <- timer.clockRealTime(MILLISECONDS)
      finish <- task >> timer.clockRealTime(MILLISECONDS)
      delay = period.toMillis - (finish - start)
      _ <- timer.sleep(delay.millis) *> repeatAtFixedRate(period, task)
    } yield ()
  }

  def repeatWithFixedDelay(delay: FiniteDuration, task: IO[Unit])(
      implicit timer: Timer[IO]): IO[Unit] = {

    task >> timer.sleep(delay) >> repeatWithFixedDelay(delay, task)
  }
}
