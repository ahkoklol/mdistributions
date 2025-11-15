package com.ahkoklol

import com.ahkoklol.config.AppConfig
import com.ahkoklol.infrastructure.db.{DoobieTransactor, PostgresEmailRepository, PostgresUserRepository}
import com.ahkoklol.infrastructure.external.{GoogleSheetsClientLive, GoogleSmtpClientLive}
import com.ahkoklol.infrastructure.utils.{BcryptHashUtility, JwtUtility}
import com.ahkoklol.domain.services.{EmailService, UserService}
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.{Console, LogLevel, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}
import zio.http.{Response, Routes, Server}
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault:

  // Layer composition for the entire application
  private val InfrastructureLayer =
    // 1. Utilities and Configuration
    AppConfig.live >+> BcryptHashUtility.live >+> JwtUtility.live

  private val DbLayer =
    InfrastructureLayer >>> AppConfig.live.select(_.db) >>> DoobieTransactor.live

  private val RepositoryLayer =
    DbLayer >>> (PostgresUserRepository.live ++ PostgresEmailRepository.live)

  private val ExternalClientLayer =
    GoogleSheetsClientLive.live ++ GoogleSmtpClientLive.live

  private val DomainServiceLayer =
    (RepositoryLayer ++ InfrastructureLayer) >>> UserService.live ++
    (RepositoryLayer ++ ExternalClientLayer) >>> EmailService.live

  // The final layer providing all dependencies required by the endpoints
  private val AppDependencies =
    DomainServiceLayer ++ JwtUtility.live

  // Overriding bootstrap to use configuration and SLF4J logging
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = 
    SLF4J.slf4j(LogLevel.Debug, LogFormat.default)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    val serverOptions: ZioHttpServerOptions[Any] =
      ZioHttpServerOptions.customiseInterceptors
        .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
        .options

    // Create the ZIO HTTP app from all Tapir server endpoints
    val app: Routes[Endpoints.ApiDependencies, Response] = ZioHttpInterpreter(serverOptions).toHttp(Endpoints.all)

    for {
      config <- ZIO.service[AppConfig.Config] // Load config to get port
      port = config.httpServer.port
      
      // Start the server
      actualPort <- Server.install(app)
      _ <- Console.printLine(s"Go to http://localhost:${actualPort}/docs to open SwaggerUI. Press ENTER key to exit.")
      _ <- Console.readLine
    } yield ()
    .provide(
      AppDependencies,
      ZLayer.succeed(Server.Config.default.port(port)),
      Server.live
    )
    .exitCode