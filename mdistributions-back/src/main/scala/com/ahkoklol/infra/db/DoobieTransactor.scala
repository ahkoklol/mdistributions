package com.ahkoklol.infra.db

import com.ahkoklol.config.DBConfig
import com.ahkoklol.config.AppConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts // <-- ADDED
import zio.*
import zio.interop.catz.*
import org.postgresql.Driver

object DoobieTransactor:
  val live: ZLayer[AppConfig, Throwable, Transactor[Task]] =
    ZLayer.scoped {
      ZIO.service[AppConfig].flatMap { config =>
        HikariTransactor.newHikariTransactor[Task](
          classOf[Driver].getName,
          config.db.url,
          config.db.user,
          config.db.pass,
          ExecutionContexts.fixedThreadPool[Task](32)
        ).toScopedZIO
      }
    }