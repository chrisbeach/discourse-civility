package uk.co.chrisbeach.perspective.db

import com.typesafe.config.Config
import doobie.imports.DriverManagerTransactor

import scalaz.effect.IO

object Transactor {
  def apply(config: Config) =
    DriverManagerTransactor[IO](
      config.getString("driver"),
      config.getString("url"),
      config.getString("username"),
      config.getString("password")
    )
}
