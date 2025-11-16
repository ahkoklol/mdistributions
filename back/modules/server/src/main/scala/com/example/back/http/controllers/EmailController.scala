package com.example.back.http.controllers

import dev.cheleb.ziotapir.SecuredBaseController

import zio.*

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

import com.example.back.domain.*
import com.example.back.http.endpoints.EmailEndpoint
import com.example.back.service.EmailService
import com.example.back.service.JWTService

class EmailController private (emailService: EmailService, jwtService: JWTService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  val createEmail: ServerEndpoint[Any, Task] = EmailEndpoint.createEmail.zServerAuthenticatedLogic { case (userId, emailDto) =>
    val emailEntity = EmailEntity(
        id = 0, // or None if using auto-generated id
        userId = userId,
        subject = emailDto.subject,
        body = emailDto.body,
        googleSheetsLink = emailDto.googleSheetsLink,
        creationDate = ZonedDateTime.now()
    )
    emailService.create(emailEntity).map(_.into[Email].transform)
    }

  val getEmailById: ServerEndpoint[Any, Task] = EmailEndpoint.getEmailById.zServerAuthenticatedLogic { case (userId, emailId) =>
    emailService.getById(emailId).map(_.map(_.into[Email].transform))
  }

  val getEmails: ServerEndpoint[Any, Task] = EmailEndpoint.getEmails.zServerAuthenticatedLogic { userId =>
    emailService.getByCreationDateDesc().map(_.filter(_.userId == userId).map(_.into[Email].transform))
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(createEmail, getEmailById, getEmails)
}

object EmailController {
  def makeZIO: URIO[EmailService & JWTService, EmailController] =
    for
      jwtService   <- ZIO.service[JWTService]
      emailService <- ZIO.service[EmailService]
    yield new EmailController(emailService, jwtService)
}