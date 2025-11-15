package com.ahkoklol.infra.utils

import com.ahkoklol.config.{AppConfig, JwtConfig}
import com.ahkoklol.domain.errors.AppError
import pdi.jwt.{JwtAlgorithm, JwtClaim as PdiJwtClaim, JwtZIOJson}
import zio.*
import zio.json.*
import java.time.Clock
import java.util.UUID

case class JwtClaim(userId: UUID)

trait JwtUtility:
  def encode(userId: UUID): IO[AppError, String]
  def validateToken(token: String): IO[AppError, JwtClaim]

object JwtUtility:
  def encode(userId: UUID): ZIO[JwtUtility, AppError, String] =
    ZIO.serviceWithZIO[JwtUtility](_.encode(userId))

  def validateToken(token: String): ZIO[JwtUtility, AppError, JwtClaim] =
    ZIO.serviceWithZIO[JwtUtility](_.validateToken(token))

case class JwtUtilityLive(config: JwtConfig, clock: Clock) extends JwtUtility:
  import com.ahkoklol.utils.JsonCodecs.given // Import codecs

  private val algo = JwtAlgorithm.HS256

  def encode(userId: UUID): IO[AppError, String] =
    ZIO.attempt { // <-- CHANGED from ZIO.succeed
      val claim = PdiJwtClaim(
        content = JwtClaim(userId).toJson,
        issuer = Some("mdistributions"),
        issuedAt = Some(clock.instant().getEpochSecond),
        expiration = Some(clock.instant().getEpochSecond + config.expiration)
      )
      JwtZIOJson.encode(claim, config.secret, algo)
    }.mapError(t => AppError.UnknownError(s"Failed to encode JWT: ${t.getMessage}"))

  def validateToken(token: String): IO[AppError, JwtClaim] =
    (for
      claimJson <- ZIO.fromTry(JwtZIOJson.decodeJson(token, config.secret, Seq(algo))) // <-- FIXED
      claim     <- ZIO.fromEither(claimJson.as[JwtClaim])
    yield claim).mapError(_ => AppError.Unauthorized("Invalid token"))

object JwtUtilityLive:
  val layer: ZLayer[AppConfig, Nothing, JwtUtility] =
    ZLayer.succeed(Clock.systemUTC()) ++ AppConfig.jwtConfig >>>
      ZLayer.fromFunction(JwtUtilityLive.apply)