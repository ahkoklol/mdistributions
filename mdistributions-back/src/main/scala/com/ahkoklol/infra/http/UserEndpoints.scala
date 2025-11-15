package com.ahkoklol.infrastructure.http

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.User
import com.ahkoklol.domain.models.User.{Login, Register, Update}
import com.ahkoklol.domain.services.UserService
import com.ahkoklol.infrastructure.http.Security.SecuredEndpoint
import com.ahkoklol.infrastructure.utils.JwtUtility
import com.ahkoklol.utils.JsonCodecs.{given, *}
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.ZServerEndpoint
import zio.ZIO

import java.util.UUID

object UserEndpoints:

  // --- Public Endpoints (No Auth) ---

  // 1. POST /user/register
  val registerEndpoint: PublicEndpoint[Register, AppError, User, Any] = endpoint.post
    .in("user" / "register")
    .in(jsonBody[Register])
    .out(jsonBody[User].description("The newly registered user object"))
    .errorOut(jsonBody[AppError])

  val registerServerEndpoint: ZServerEndpoint[UserService, Any] = registerEndpoint.serverLogic { registerData =>
    ZIO.serviceWithZIO[UserService](_.register(registerData))
  }

  // 2. POST /user/login
  val loginEndpoint: PublicEndpoint[Login, AppError, (User, String), Any] = endpoint.post
    .in("user" / "login")
    .in(jsonBody[Login])
    .out(jsonBody[User].description("The authenticated user object"))
    .out(header[String]("X-Auth-Token").description("JWT for subsequent requests"))
    .errorOut(jsonBody[AppError])

  val loginServerEndpoint: ZServerEndpoint[UserService & JwtUtility, Any] = loginEndpoint.serverLogic { loginData =>
    for {
      userService <- ZIO.service[UserService]
      jwtUtility <- ZIO.service[JwtUtility]
      user <- userService.login(loginData)
      token <- jwtUtility.issueToken(user.id)
    } yield (user, token)
  }

  // --- Secured Endpoints (Requires Auth) ---

  // 3. GET /user/me
  // Fetches the current user's data based on the authenticated JWT
  val getMeEndpoint: SecuredEndpoint[Unit, User] = Security.secureEndpoint.get
    .in("user" / "me")
    .out(jsonBody[User].description("The authenticated user's profile"))

  val getMeServerEndpoint: SecuredEndpoint[Unit, User] = getMeEndpoint.serverLogic { userId => _ =>
    ZIO.serviceWithZIO[UserService](_.getUser(userId))
  }

  // 4. PUT /user/me
  // Updates the current user's profile
  val updateMeEndpoint: SecuredEndpoint[Update, Unit] = Security.secureEndpoint.put
    .in("user" / "me")
    .in(jsonBody[Update])
    .out(statusCode(sttp.model.StatusCode.NoContent))

  val updateMeServerEndpoint: SecuredEndpoint[Update, Unit] = updateMeEndpoint.serverLogic { userId => updateData =>
    ZIO.serviceWithZIO[UserService](_.update(userId, updateData))
  }

  // 5. DELETE /user/me
  // Deletes the current user's account
  val deleteMeEndpoint: SecuredEndpoint[Unit, Unit] = Security.secureEndpoint.delete
    .in("user" / "me")
    .out(statusCode(sttp.model.StatusCode.NoContent))

  val deleteMeServerEndpoint: SecuredEndpoint[Unit, Unit] = deleteMeEndpoint.serverLogic { userId => _ =>
    ZIO.serviceWithZIO[UserService](_.delete(userId))
  }

  val all: List[ZServerEndpoint[UserService & JwtUtility, Any]] = List(
    registerServerEndpoint,
    loginServerEndpoint,
    getMeServerEndpoint,
    updateMeServerEndpoint,
    deleteMeServerEndpoint
  )