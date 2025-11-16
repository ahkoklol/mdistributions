package com.example.back.service

import zio.*
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase

object PostgresTestcontainer {

  val live: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase]] =
    ZPostgreSQLContainer.Settings.default >>>
      ZPostgreSQLContainer.live >>>   // provides ZPostgreSQLContainer
      ZLayer.fromZIO {
        // access the DataSource directly from the container
        ZIO.service[ZPostgreSQLContainer].map(container =>
          Quill.Postgres(SnakeCase, container.dataSource)
        )
      }
}
