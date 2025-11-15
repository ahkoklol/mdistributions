package com.ahkoklol.config

import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigSource
import zio.config.typesafe.fromResourcePath
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

  final case class AppConfiguration(
      db: DBConfig,
      httpServer: HttpServerConfig,
      jwt: JwtConfig
  )

  given dbConfig: Config[DBConfig] = deriveConfig[DBConfig]
  given httpConfig: Config[HttpServerConfig] = deriveConfig[HttpServerConfig]
  given jwtConfig: Config[JwtConfig] = deriveConfig[JwtConfig]
  given appConfig: Config[AppConfiguration] = deriveConfig[AppConfiguration]

  val live: ZLayer[Any, Config.Error, AppConfiguration] = 
    ZLayer.fromZIO(
      fromResourcePath.load(appConfig)
    )