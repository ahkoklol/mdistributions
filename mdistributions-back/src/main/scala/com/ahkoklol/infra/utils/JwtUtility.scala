package com.ahkoklol.infrastructure.utils

import com.ahkoklol.config.AppConfig.JwtConfig
import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.errors.AppError.AuthenticationError
import zio.{IO, ZIO, ZLayer}
import pdi.jwt.{JwtAlgorithm, JwtZIOJson}
import zio.json.*

import java.time.Clock
import java.util.UUID

trait JwtUtility:
  /** Issues a new JWT for a given user ID. */
  def issueToken(userId: UUID): IO[AppError, String]
  
  /** Validates and decodes a JWT, returning the user ID. */
  def validateToken(token: String): IO[AppError, UUID]

object JwtUtility:
  val live: ZLayer[JwtConfig, Nothing, JwtUtility] = ZLayer.fromFunction { (config: JwtConfig) =>
    new JwtUtility:
      // Required by pdi.jwt for expiration claims
      private implicit val clock: Clock = Clock.systemUTC()
      private val algo = JwtAlgorithm.HS256
      private val secret = config.secret
      private val expiration = config.expirationInHours * 3600 // hours to seconds

      override def issueToken(userId: UUID): IO[AppError, String] =
        ZIO.succeed {
          val claims = s"""{"userId": "${userId.toString}"}""".fromJson[Json]
          JwtZIOJson.encode(claims.getOrElse(Json.Null), secret, algo)
        }

      override def validateToken(token: String): IO[AppError, UUID] =
        JwtZIOJson.decode(token, secret, Seq(algo))
          .mapError(e => AuthenticationError(s"Invalid token: ${e.getMessage}"))
          .flatMap { claims =>
            claims.content.fromJson[Map[String, String]] match
              case Right(map) if map.contains("userId") => ZIO.attempt(UUID.fromString(map("userId")))
              case _ => ZIO.fail(AuthenticationError("Invalid token claims format."))
          }
          .mapError(e => e.fold(_ => e, identity))
  }
