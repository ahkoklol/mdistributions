package com.ahkoklol.config

import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider
import zio.ZLayer

// Define component-specific configurations
case class DBConfig(url: String, user: String, pass: String)
case class HttpServerConfig(host: String, port: Int)
case class JwtConfig(secret: String, expiration: Long)

// Define the top-level application configuration
case class AppConfig(
    db: DBConfig,
    http: HttpServerConfig,
    jwt: JwtConfig
)

object AppConfig:
  // Derive the decoder for the AppConfig case class
  private val configDescriptor: ConfigDescriptor[AppConfig] =
    deriveConfig[AppConfig].nested("AppConfig") // Assumes config is under an "AppConfig" block

  // Define the live ZLayer for providing the configuration
  val live: ZLayer[Any, zio.Config.Error, AppConfig] =
    ZLayer.fromZIO(
      read(
        configDescriptor.from(
          TypesafeConfigProvider.fromResourcePath()
        )
      )
    )

  // Provide helper layers to access nested config parts
  val dbConfig: ZLayer[AppConfig, Nothing, DBConfig] =
    ZLayer.service[AppConfig].project(_.db)

  val httpConfig: ZLayer[AppConfig, Nothing, HttpServerConfig] =
    ZLayer.service[AppConfig].project(_.http)

  val jwtConfig: ZLayer[AppConfig, Nothing, JwtConfig] =
    ZLayer.service[AppConfig].project(_.jwt)
    
end AppConfig