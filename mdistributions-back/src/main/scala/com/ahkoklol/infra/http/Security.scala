package com.ahkoklol.infrastructure.http

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.infrastructure.utils.JwtUtility
import sttp.tapir.*
import sttp.tapir.ztapir.ZServerEndpoint
import zio.ZIO

import java.util.UUID

object Security:
  
  // This is the core Tapir Input that extracts and validates the JWT
  val secureEndpoint: Endpoint[String, UUID, AppError, Unit, Any] = endpoint
    .securityIn(auth.bearer[String]()) // Expect a Bearer token in the Authorization header
    .errorOut(jsonBody[AppError]) // All authentication failures return an AppError
    .errorOut(statusCode)
    .in(extractFromSecurity("userId", () => UUID.randomUUID())) // Placeholder output type
    .mapSecurityIn[UUID] { token =>
      JwtUtility.validateToken(token)
    }(_.toString) // Dummy reverse mapping for documentation (not used at runtime)
    .mapErrorOut[AppError](identity)(identity)
    .serverSecurityLogic { token =>
      ZIO.serviceWithZIO[JwtUtility](_.validateToken(token)).as(Right(_))
    }
  
  // A helper type for endpoints that require authentication
  type SecuredEndpoint[I, O] = ZServerEndpoint[JwtUtility, I, AppError, O, Any]
