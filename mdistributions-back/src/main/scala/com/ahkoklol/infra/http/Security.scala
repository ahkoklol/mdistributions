package com.ahkoklol.infrastructure.http

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.infrastructure.utils.JwtUtility
import com.ahkoklol.utils.JsonCodecs.{given, *}
import sttp.tapir.*
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir.json.zio.jsonBody
import zio.ZIO

import java.util.UUID

object Security:
  
  // Dependencies required by the security layer (JWT validation)
  type SecurityDeps = JwtUtility

  // This is the core Tapir Input that extracts and validates the JWT
  val secureEndpoint: Endpoint[String, UUID, AppError, Unit, Any] = endpoint
    .securityIn(auth.bearer[String]())
    .errorOut(jsonBody[AppError]) 
    .errorOut(statusCode)
    .out(emptyOutput) 
    .mapSecurityIn[UUID] { token =>
      ZIO.serviceWithZIO[JwtUtility](_.validateToken(token)).as(Right(_))
    }(_.toString)
    .serverSecurityLogic { token =>
      ZIO.serviceWithZIO[JwtUtility](_.validateToken(token)).map(Right(_))
    }
  
  type SecuredEndpoint[I, O] = ZServerEndpoint[SecurityDeps, I, AppError, O, Any]