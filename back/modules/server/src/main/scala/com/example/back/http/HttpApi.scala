package com.example.back.http

import zio.*
import sttp.tapir.server.ServerEndpoint
import sttp.capabilities.zio.ZioStreams

import dev.cheleb.ziotapir.*

import controllers.*

import com.example.back.service.*

object HttpApi extends Routes {

  private def makeControllers = for {
    healthController <- HealthController.makeZIO
    emailController <- EmailController.makeZIO
    userController  <- UserController.makeZIO
  } yield List(healthController, emailController, userController)

  def endpointsZIO: URIO[EmailService & UserService & JWTService, List[ServerEndpoint[Any, Task]]] =
    makeControllers.map(gatherRoutes(_.routes))

  def streamEndpointsZIO: URIO[EmailService & UserService & JWTService, List[ServerEndpoint[ZioStreams, Task]]] =
    makeControllers.map(gatherRoutes(_.streamRoutes))

  def endpoints = for {
    endpoints       <- endpointsZIO
    streamEndpoints <- streamEndpointsZIO
  } yield endpoints ++ streamEndpoints
}
