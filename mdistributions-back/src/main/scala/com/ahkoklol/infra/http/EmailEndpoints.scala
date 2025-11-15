package com.ahkoklol.infrastructure.http

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.EmailDraft
import com.ahkoklol.domain.models.EmailDraft.Save
import com.ahkoklol.domain.services.EmailService
import com.ahkoklol.domain.services.UserService
import com.ahkoklol.infrastructure.http.Security.SecuredEndpoint
import com.ahkoklol.utils.JsonCodecs.{given, *}
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.ZServerEndpoint
import zio.ZIO

import java.util.UUID

object EmailEndpoints:

  // --- Secured Endpoints (Requires Auth) ---

  // 1. POST /emails/draft
  val saveDraftEndpoint: SecuredEndpoint[Save, EmailDraft] = Security.secureEndpoint.post
    .in("emails" / "draft")
    .in(jsonBody[Save].description("Email subject, body, and sheets link"))
    .out(jsonBody[EmailDraft].description("The saved draft"))

  val saveDraftServerEndpoint: SecuredEndpoint[Save, EmailDraft] = saveDraftEndpoint.serverLogic { userId => saveDraftData =>
    ZIO.serviceWithZIO[EmailService](_.saveDraft(userId, saveDraftData))
  }

  // 2. GET /emails/draft/{id}
  val getDraftEndpoint: SecuredEndpoint[UUID, EmailDraft] = Security.secureEndpoint.get
    .in("emails" / "draft" / path[UUID]("draftId"))
    .out(jsonBody[EmailDraft].description("The requested draft"))

  val getDraftServerEndpoint: SecuredEndpoint[UUID, EmailDraft] = getDraftEndpoint.serverLogic { userId => draftId =>
    ZIO.serviceWithZIO[EmailService](_.getDraft(draftId, userId))
  }

  // 3. POST /emails/send/{id}
  val sendEmailEndpoint: SecuredEndpoint[UUID, Unit] = Security.secureEndpoint.post
    .in("emails" / "send" / path[UUID]("draftId"))
    .out(statusCode(sttp.model.StatusCode.Accepted))

  val sendEmailServerEndpoint: ZServerEndpoint[EmailService & UserService & Security.SecurityDeps, UUID, AppError, Unit, Any] =
    sendEmailEndpoint.serverLogic { userId => draftId =>
      // Need to retrieve the full User object to access SMTP credentials
      for {
        userService <- ZIO.service[UserService]
        emailService <- ZIO.service[EmailService]
        user <- userService.getUser(userId)
        _ <- emailService.sendEmail(draftId, user)
      } yield ()
    }

  val all: List[ZServerEndpoint[EmailService & UserService & Security.SecurityDeps, Any]] = List(
    saveDraftServerEndpoint,
    getDraftServerEndpoint,
    sendEmailServerEndpoint
  )
