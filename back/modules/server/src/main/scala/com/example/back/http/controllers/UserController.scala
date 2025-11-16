package com.example.back.http.controllers

import dev.cheleb.ziotapir.SecuredBaseController

import zio.*

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

import com.example.back.domain.*
import com.example.back.http.endpoints.UserEndpoint
import com.example.back.service.UserService
import com.example.back.service.JWTService

class UserController private (userService: UserService, jwtService: JWTService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  val register: ServerEndpoint[Any, Task] = UserEndpoint.register
    .zServerLogic { dto: RegisterUser =>
        userService.register(dto.name, dto.email, dto.password, dto.googleSheetsLink)
    }


  val login: ServerEndpoint[Any, Task] = UserEndpoint.login.zServerLogic { lp: LoginPassword =>
    for {
        user  <- userService.login(lp.email, lp.password)
        token <- jwtService.createToken(user)
    } yield token
    }

  val profile: ServerEndpoint[Any, Task] = UserEndpoint.profile.zServerAuthenticatedLogic { userId: String =>
    userService.getUserById(userId.toLong)
  }

  val update: ServerEndpoint[Any, Task] = UserEndpoint.update.zServerAuthenticatedLogic { case (userId: String, op: UpdateUserOp) =>
    userService.updateUser(userId.toLong, user => 
        user.copy(
        name = op.name.getOrElse(user.name),
        googleSheetsLink = op.googleSheetsLink.orElse(user.googleSheetsLink)
        )
    )
    }

    val delete: ServerEndpoint[Any, Task] = UserEndpoint.delete.zServerAuthenticatedLogic { userId: String =>
        userService.deleteUser(userId.toLong)
    }

  override val routes: List[ServerEndpoint[Any, Task]] = List(register, login, profile, update, delete)
}

object UserController {
  def makeZIO: URIO[UserService & JWTService, UserController] =
    for
      jwtService  <- ZIO.service[JWTService]
      userService <- ZIO.service[UserService]
    yield new UserController(userService, jwtService)
}