package uk.co.chrisbeach.perspective.util

import scala.concurrent.duration.FiniteDuration

object JavaTimeConversion {
  implicit def toFiniteDuration(d: FiniteDuration): java.time.Duration =
    java.time.Duration.ofNanos(d.toNanos)
}
