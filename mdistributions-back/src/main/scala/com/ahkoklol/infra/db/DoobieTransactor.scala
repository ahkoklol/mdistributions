package com.ahkoklol.infrastructure.db

import com.ahkoklol.config.AppConfig.DBConfig // Assuming DBConfig is defined here
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import zio.interop.catz.*
import zio.{Task, ZIO, ZLayer}

object DoobieTransactor:
  type Transactor = HikariTransactor[Task]

  // Layer to create the Hikari connection pool and transactor
  val live: ZLayer[DBConfig, Throwable, Transactor] = 
    ZLayer.scoped {
      for {
        dbConfig <- ZIO.service[DBConfig]
        // Necessary for Doobie to use ZIO's concurrency model
        ce <- ExecutionContexts.fixedThreadPool[Task](32).toScopedZIO
        xa <- HikariTransactor.newHikariTransactor[Task](
                driverClassName = "org.postgresql.Driver",
                url = dbConfig.url,
                user = dbConfig.user,
                pass = dbConfig.password,
                ce
              ).toScopedZIO
      } yield xa
    }
