package com.ahkoklol.config

import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigSource
import zio.{Config, ZLayer}

object AppConfig:

  final case class DBConfig(
      url: String,
      user: String,
      password: String
  )

  final case class HttpServerConfig(
      port: Int
  )

  final case class JwtConfig(
      secret: String,
      expirationInHours: Int
  )

  final case class Config(
      db: DBConfig,
      httpServer: HttpServerConfig,
      jwt: JwtConfig
  )

  // Use ZIO Config to derive config from case classes
  given dbConfig: Config[DBConfig] = deriveConfig[DBConfig]
  given httpConfig: Config[HttpServerConfig] = deriveConfig[HttpServerConfig]
  given jwtConfig: Config[JwtConfig] = deriveConfig[JwtConfig]
  given config: Config[Config] = deriveConfig[Config]

  // Layer that loads configuration from an application.conf file
  val live: ZLayer[Any, Config.Error, Config] = 
    ZLayer.fromZIO(
      TypesafeConfigSource.fromResourcePath
        .load(Config.collectAll(dbConfig, httpConfig, jwtConfig))
    )
