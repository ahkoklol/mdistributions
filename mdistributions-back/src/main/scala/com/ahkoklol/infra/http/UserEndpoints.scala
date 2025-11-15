package com.ahkoklol.infrastructure.http

import com.ahkoklol.domain.models.User.Login
import com.ahkoklol.domain.services.UserService
import com.ahkoklol.infrastructure.utils.JwtUtility // NEW
import com.ahkoklol.utils.JsonCodecs.{given, *} // JSON Codecs
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.ZServerEndpoint
import zio.ZIO

object UserEndpoints:
    
  val loginEndpoint: PublicEndpoint[Login, AppError, (User, String), Any] = endpoint.post
    .in("user" / "login")
    .in(jsonBody[Login])
    .out(jsonBody[User].and(header[String]("X-Auth-Token"))) // Returns User + JWT in header
    .errorOut(jsonBody[AppError])

  val loginServerEndpoint: ZServerEndpoint[UserService & JwtUtility, Any] = loginEndpoint.serverLogic { loginData =>
    ZIO.serviceWithZIO[UserService](_.login(loginData))
      .flatMap { user =>
        ZIO.serviceWithZIO[JwtUtility](_.issueToken(user.id))
          .map(token => (user, token))
      }
      .mapBoth(identity, identity)
  }
  
  // ... other endpoints like register, update, delete ...
