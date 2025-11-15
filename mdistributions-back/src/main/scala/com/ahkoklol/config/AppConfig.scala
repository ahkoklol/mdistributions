package com.ahkoklol.config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider

case class DBConfig(url: String, user: String, pass: String)
case class HttpServerConfig(host: String, port: Int)
case class JwtConfig(secret: String, expiration: Long)

case class AppConfig(
    db: DBConfig,
    http: HttpServerConfig,
    jwt: JwtConfig
)

object AppConfig:

  // ZIO 2.x config descriptor
  private val configDescriptor: Config[AppConfig] =
    deriveConfig[AppConfig].nested("AppConfig")

  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(
      TypesafeConfigProvider
        .fromResourcePath()
        .load(configDescriptor)
    )

  val dbConfig: ZLayer[AppConfig, Nothing, DBConfig] =
    ZLayer.fromFunction((c: AppConfig) => c.db)

  val httpConfig: ZLayer[AppConfig, Nothing, HttpServerConfig] =
    ZLayer.fromFunction((c: AppConfig) => c.http)

  val jwtConfig: ZLayer[AppConfig, Nothing, JwtConfig] =
    ZLayer.fromFunction((c: AppConfig) => c.jwt)

end AppConfig
