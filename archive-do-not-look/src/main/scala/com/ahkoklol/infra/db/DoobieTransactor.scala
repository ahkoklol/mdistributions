package com.ahkoklol.infra.db

import com.ahkoklol.config.AppConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*
import scala.concurrent.ExecutionContext
import org.postgresql.Driver

object DoobieTransactor:

  val live: ZLayer[AppConfig, Throwable, Transactor[Task]] =
    ZLayer.scoped {
      for
        config <- ZIO.service[AppConfig]

        // Create a fixed thread pool for DB connections
        connectEC <- ZIO.attempt(ExecutionContext.fromExecutor(
          java.util.concurrent.Executors.newFixedThreadPool(32)
        ))

        // Create HikariTransactor (returns cats.effect.Resource)
        transactor <- HikariTransactor
          .newHikariTransactor[Task](
            driverClassName = classOf[Driver].getName,
            url             = config.db.url,
            user            = config.db.user,
            pass            = config.db.pass,
            connectEC       = connectEC
          )
          .toScopedZIO // <-- convert cats Resource → ZIO scoped
      yield transactor
    }
