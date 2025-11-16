package com.ahkoklol.infra.http

import com.ahkoklol.domain.models.*
import com.ahkoklol.domain.services.UserService
import com.ahkoklol.infra.http.Security.{publicEndpoint, securedEndpoint, SecurityDeps}
import com.ahkoklol.utils.JsonCodecs.given
import sttp.tapir.ztapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.given
import sttp.model.StatusCode
import zio.ZIO
import sttp.tapir.server.ziohttp.ZioHttpInterpreter._

object UserEndpoints:
  type UserEndpointsEnv = UserService & SecurityDeps

  // POST /users/register
  val registerEndpoint = publicEndpoint.post
    .in("users" / "register")
    .in(jsonBody[User.Register])
    .out(jsonBody[User])
    .out(statusCode(StatusCode.Created))

  val registerServerEndpoint: ZServerEndpoint[UserEndpointsEnv, Any] =
    registerEndpoint.zServerLogic { registerData =>
      ZIO.serviceWithZIO[UserService](_.register(registerData))
    }

  // POST /users/login
  val loginEndpoint = publicEndpoint.post
    .in("users" / "login")
    .in(jsonBody[User.Login])
    .out(jsonBody[(User, String)]) // (User, Token)

  val loginServerEndpoint: ZServerEndpoint[UserEndpointsEnv, Any] =
    loginEndpoint.zServerLogic { loginData =>
      ZIO.serviceWithZIO[UserService](_.login(loginData))
    }

  // GET /user/me
  val getMeEndpoint = securedEndpoint.get
    .in("user" / "me")
    .out(jsonBody[User])

  val getMeServerEndpoint: ZServerEndpoint[UserEndpointsEnv, Any] =
    getMeEndpoint.serverLogic { userId => _ =>
      ZIO.serviceWithZIO[UserService](_.findById(userId))
    }

  // PUT /user/me
  val updateMeEndpoint = securedEndpoint.put
    .in("user" / "me")
    .in(jsonBody[User.Update])
    .out(statusCode(StatusCode.NoContent))

  val updateMeServerEndpoint: ZServerEndpoint[UserEndpointsEnv, Any] =
    updateMeEndpoint.serverLogic { userId => updateData =>
      ZIO.serviceWithZIO[UserService](_.update(userId, updateData))
    }

  // DELETE /user/me
  val deleteMeEndpoint = securedEndpoint.delete
    .in("user" / "me")
    .out(statusCode(StatusCode.NoContent))

  val deleteMeServerEndpoint: ZServerEndpoint[UserEndpointsEnv, Any] =
    deleteMeEndpoint.serverLogic { userId => _ =>
      ZIO.serviceWithZIO[UserService](_.delete(userId))
    }

  // Combine all user endpoints
  val all: List[ZServerEndpoint[UserEndpointsEnv, Any]] = List(
    registerServerEndpoint,
    loginServerEndpoint,
    getMeServerEndpoint,
    updateMeServerEndpoint,
    deleteMeServerEndpoint
  )

end UserEndpoints
