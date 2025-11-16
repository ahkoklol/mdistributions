package com.example.back.service

import zio.*
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import javax.sql.DataSource

object PostgresTestcontainer {

  val live: ZLayer[Any, Throwable, Quill.PostgresLite[SnakeCase]] =
    ZPostgreSQLContainer.Settings.default >>>
      ZPostgreSQLContainer.live >>>
      dataSourceToQuill

  private val dataSourceToQuill: ZLayer[DataSource, Throwable, Quill.PostgresLite[SnakeCase]] =
    ZLayer.scoped {
      ZIO.service[DataSource].map(ds => Quill.Postgres(SnakeCase, ds))
    }
}
