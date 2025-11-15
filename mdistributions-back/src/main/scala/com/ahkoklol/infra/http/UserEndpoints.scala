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
  // ... loginEndpoint definition ...
  val loginServerEndpoint: ZServerEndpoint[UserService & JwtUtility, Any] = loginEndpoint.serverLogic { loginData =>
    for {
      userService <- ZIO.service[UserService]
      jwtUtility <- ZIO.service[JwtUtility]
      user <- userService.login(loginData)
      token <- jwtUtility.issueToken(user.id)
    } yield Right((user, token)) // FIX: Tapir requires Right(Output)
  }

  // --- Secured Endpoints (Requires Auth) ---

  // 3. GET /user/me
  val getMeEndpoint: SecuredEndpoint[Unit, User] = Security.secureEndpoint.get
    .in("user" / "me")
    .in(emptyInput) // FIX: Add explicit Unit input for cleaner logic
    .out(jsonBody[User].description("The authenticated user's profile"))

  val getMeServerEndpoint: ZServerEndpoint[UserService & SecurityDeps, Unit, AppError, User, Any] = getMeEndpoint.serverLogic { userId => _ => // FIX: Secured logic signature: (Security, Input) => ZIO
    ZIO.serviceWithZIO[UserService](_.getUser(userId)).map(Right(_)) // FIX: Returns Right(Output)
  }

  // 4. PUT /user/me
  val updateMeEndpoint: SecuredEndpoint[Update, Unit] = Security.secureEndpoint.put
    .in("user" / "me")
    .in(jsonBody[Update])
    .out(statusCode(sttp.model.StatusCode.NoContent))

  val updateMeServerEndpoint: ZServerEndpoint[UserService & SecurityDeps, Update, AppError, Unit, Any] = updateMeEndpoint.serverLogic { userId => updateData =>
    ZIO.serviceWithZIO[UserService](_.update(userId, updateData)).map(Right(_)) // FIX: Returns Right(Output)
  }

  // 5. DELETE /user/me
  val deleteMeEndpoint: SecuredEndpoint[Unit, Unit] = Security.secureEndpoint.delete
    .in("user" / "me")
    .in(emptyInput) // FIX: Add explicit Unit input
    .out(statusCode(sttp.model.StatusCode.NoContent))

  val deleteMeServerEndpoint: ZServerEndpoint[UserService & SecurityDeps, Unit, AppError, Unit, Any] = deleteMeEndpoint.serverLogic { userId => _ =>
    ZIO.serviceWithZIO[UserService](_.delete(userId)).map(Right(_)) // FIX: Returns Right(Output)
  }

  val all: List[ZServerEndpoint[UserService & JwtUtility, Any]] = List(
    registerServerEndpoint.asInstanceOf[ZServerEndpoint[UserService & JwtUtility, Any]], // Casting to match required environment
    loginServerEndpoint,
    getMeServerEndpoint.asInstanceOf[ZServerEndpoint[UserService & JwtUtility, Any]],
    updateMeServerEndpoint.asInstanceOf[ZServerEndpoint[UserService & JwtUtility, Any]],
    deleteMeServerEndpoint.asInstanceOf[ZServerEndpoint[UserService & JwtUtility, Any]]
  )