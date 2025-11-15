package com.ahkoklol.infra.http

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.infra.utils.JwtUtility
import com.ahkoklol.utils.JsonCodecs.given // Import all JSON codecs
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.* // For AppError schema
import zio.ZIO
import java.util.UUID

object Security:
  // This is the dependency required for security
  type SecurityDeps = JwtUtility

  // This is a partial server endpoint that handles security.
  // It takes a String (token), validates it, and provides a UUID (userId)
  // or returns an AppError.
  val securedEndpoint: ZPartialServerEndpoint[SecurityDeps, String, UUID, Unit, AppError, Unit, Any] =
    endpoint
      .securityIn(auth.bearer[String]())
      .errorOut(jsonBody[AppError])
      .zServerSecurityLogic { token =>
        JwtUtility.validateToken(token).map(_.userId)
      }

  // A public endpoint (no security) that can fail with AppError
  val publicEndpoint: ZPublicEndpoint[Unit, AppError, Unit, Any] =
    endpoint
      .errorOut(jsonBody[AppError])

end Security