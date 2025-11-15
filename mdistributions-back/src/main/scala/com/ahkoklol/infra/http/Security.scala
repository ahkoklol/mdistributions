package com.ahkoklol.infra.http

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.infra.utils.JwtUtility
import com.ahkoklol.utils.JsonCodecs.given
import sttp.tapir._
import sttp.tapir.ztapir._
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.given
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.ZIO
import java.util.UUID

object Security:
  type SecurityDeps = JwtUtility

  // Secured endpoint
  val securedEndpoint: ZPartialServerEndpoint[SecurityDeps, String, UUID, Unit, AppError, Unit, Any] =
  sttp.tapir.ztapir.endpoint
    .securityIn(auth.bearer[String]())
    .errorOut(jsonBody[AppError])
    .zServerSecurityLogic { token =>
      JwtUtility.validateToken(token).map(_.userId)
    }

    val publicEndpoint: PublicEndpoint[Unit, AppError, Unit, Any] =
    sttp.tapir.ztapir.endpoint.errorOut(jsonBody[AppError])


end Security
