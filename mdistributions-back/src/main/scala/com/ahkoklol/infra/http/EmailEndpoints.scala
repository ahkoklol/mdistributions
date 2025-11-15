package com.ahkoklol.infrastructure.http

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.EmailDraft
import com.ahkoklol.domain.models.EmailDraft.Save
import com.ahkoklol.domain.services.{EmailService, UserService}
import com.ahkoklol.infrastructure.http.Security.{SecuredEndpoint, SecurityDeps}
import com.ahkoklol.utils.JsonCodecs.{given, *}
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.generic.auto.* // FIX: Schema derivation import
import sttp.tapir.ztapir.ZServerEndpoint
import zio.ZIO

import java.util.UUID

object EmailEndpoints:

  // 1. POST /emails/draft
  val saveDraftEndpoint: SecuredEndpoint[Save, EmailDraft] = Security.secureEndpoint.post
    .in("emails" / "draft")
    .in(jsonBody[Save].description("Email subject, body, and sheets link"))
    .out(jsonBody[EmailDraft].description("The saved draft"))

  val saveDraftServerEndpoint: ZServerEndpoint[EmailService & SecurityDeps, Save, AppError, EmailDraft, Any] = saveDraftEndpoint.serverLogic { userId => saveDraftData =>
    ZIO.serviceWithZIO[EmailService](_.saveDraft(userId, saveDraftData)).map(Right(_)) // FIX: Returns Right(Output)
  }

  // 2. GET /emails/draft/{id}
  val getDraftEndpoint: SecuredEndpoint[UUID, EmailDraft] = Security.secureEndpoint.get
    .in("emails" / "draft" / path[UUID]("draftId"))
    .out(jsonBody[EmailDraft].description("The requested draft"))

  val getDraftServerEndpoint: ZServerEndpoint[EmailService & SecurityDeps, UUID, AppError, EmailDraft, Any] = getDraftEndpoint.serverLogic { userId => draftId =>
    ZIO.serviceWithZIO[EmailService](_.getDraft(draftId, userId)).map(Right(_)) // FIX: Returns Right(Output)
  }

  // 3. POST /emails/send/{id}
  val sendEmailEndpoint: SecuredEndpoint[UUID, Unit] = Security.secureEndpoint.post
    .in("emails" / "send" / path[UUID]("draftId"))
    .out(statusCode(sttp.model.StatusCode.Accepted))

  // FIX: Reduced ZServerEndpoint arguments to the standard [R, C] format
  val sendEmailServerEndpoint: ZServerEndpoint[EmailService & UserService & SecurityDeps, UUID, AppError, Unit, Any] =
    sendEmailEndpoint.serverLogic { userId => draftId =>
      for {
        userService <- ZIO.service[UserService]
        emailService <- ZIO.service[EmailService]
        user <- userService.getUser(userId)
        _ <- emailService.sendEmail(draftId, user)
      } yield Right(()) // FIX: Returns Right(Output)
    }

  val all: List[ZServerEndpoint[EmailService & UserService & SecurityDeps, Any]] = List(
    saveDraftServerEndpoint,
    getDraftServerEndpoint,
    sendEmailServerEndpoint
  )