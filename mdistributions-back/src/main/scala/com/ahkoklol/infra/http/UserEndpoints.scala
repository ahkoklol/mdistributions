package com.ahkoklol.infra.http

import com.ahkoklol.domain.models.*
import com.ahkoklol.domain.services.UserService
import com.ahkoklol.infra.http.Security.{publicEndpoint, securedEndpoint, SecurityDeps}
import com.ahkoklol.utils.JsonCodecs.given // Import all JSON codecs
import sttp.tapir.ztapir.*
import sttp.tapir.ztapir.RichZServerEndpoint // <-- ADDED for .zServerLogic
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.given // Import auto-derivation for schemas
import sttp.model.StatusCode
import zio.ZIO

object UserEndpoints:
  type UserEndpointsEnv = UserService & SecurityDeps

  // POST /users/register
  val registerEndpoint = publicEndpoint.post
    .in("users" / "register")
    .in(jsonBody[User.Register])
    .out(jsonBody[User])
    .out(statusCode(StatusCode.Created))

  val registerServerEndpoint = registerEndpoint.zServerLogic { registerData =>
    UserService.register(registerData)
  }

  // POST /users/login
  val loginEndpoint = publicEndpoint.post
    .in("users" / "login")
    .in(jsonBody[User.Login])
    .out(jsonBody[(User, String)]) // (User, Token)

  val loginServerEndpoint = loginEndpoint.zServerLogic { loginData =>
    UserService.login(loginData)
  }

  // GET /user/me
  val getMeEndpoint = securedEndpoint.get
    .in("user" / "me")
    .out(jsonBody[User])

  val getMeServerEndpoint = getMeEndpoint.zServerLogic { userId => _ => // (userId, Unit)
    UserService.findById(userId)
  }

  // PUT /user/me
  val updateMeEndpoint = securedEndpoint.put
    .in("user" / "me")
    .in(jsonBody[User.Update])
    .out(statusCode(StatusCode.NoContent))

  val updateMeServerEndpoint = updateMeEndpoint.zServerLogic { userId => updateData => // (userId, Update)
    UserService.update(userId, updateData)
  }

  // DELETE /user/me
  val deleteMeEndpoint = securedEndpoint.delete
    .in("user" / "me")
    .out(statusCode(StatusCode.NoContent))

  val deleteMeServerEndpoint = deleteMeEndpoint.zServerLogic { userId => _ => // (userId, Unit)
    UserService.delete(userId)
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