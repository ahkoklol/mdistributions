package com.ahkoklol

import com.ahkoklol.config.AppConfig
import com.ahkoklol.domain.ports.*
import com.ahkoklol.domain.services.*
import com.ahkoklol.infra.db.*
import com.ahkoklol.infra.external.*
import com.ahkoklol.infra.utils.JwtUtilityLive
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.{Console, ExitCode, LogLevel, Runtime, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer} // <-- Added Runtime
import zio.http.{Response, Routes, Server}
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault:

  // Corrected bootstrap layer
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers ++ SLF4J.slf4j(LogLevel.Debug, LogFormat.default) // <-- CORRECTED

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =
    val serverOptions: ZioHttpServerOptions[Any] =
      ZioHttpServerOptions.customiseInterceptors
        .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
        .options

    // The app now requires our AppDependencies
    val app: Routes[Endpoints.AppDependencies, Response] =
      ZioHttpInterpreter(serverOptions).toHttp(Endpoints.all)

    val serverProgram =
      for
        config     <- ZIO.service[AppConfig]
        actualPort <- Server.install(app)
        _          <- Console.printLine(s"Go to http://localhost:${actualPort}/docs to open SwaggerUI. Press ENTER key to exit.")
        _          <- Console.readLine
      yield ()

    serverProgram.provide(
      // Configuration
      AppConfig.live,
      AppConfig.dbConfig, 
      AppConfig.httpConfig,
      
      // Infrastructure Layers
      DoobieTransactor.live,
      PostgresUserRepository.live,
      PostgresEmailRepository.live,
      GoogleSheetsClientLive.layer,
      GoogleSmtpClientLive.layer,
      JwtUtilityLive.layer,

      // Service (Business Logic) Layers
      UserServiceLive.layer,
      EmailServiceLive.layer,

      // ZIO HTTP Server
      Server.live,
      ZLayer.service[AppConfig].project(c => Server.Config.default.port(c.http.port))
    )