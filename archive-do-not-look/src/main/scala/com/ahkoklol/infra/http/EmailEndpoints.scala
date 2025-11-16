package com.ahkoklol.infra.http

import com.ahkoklol.domain.models.*
import com.ahkoklol.domain.services.EmailService
import com.ahkoklol.infra.http.Security.{securedEndpoint, SecurityDeps}
import com.ahkoklol.utils.JsonCodecs.given // Import all JSON codecs
import sttp.tapir.ztapir.*
import sttp.tapir.ztapir.RichZServerEndpoint // <-- ADDED for .zServerLogic
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.given // Import auto-derivation for schemas
import sttp.model.StatusCode
import zio.ZIO
import java.util.UUID

object EmailEndpoints:
  type EmailEndpointsEnv = EmailService & SecurityDeps

  // POST /emails/drafts
  val saveDraftEndpoint = securedEndpoint.post
    .in("emails" / "drafts")
    .in(jsonBody[EmailDraft.Save])
    .out(jsonBody[EmailDraft])
    .out(statusCode(StatusCode.Created))

  val saveDraftServerEndpoint = saveDraftEndpoint.serverLogic { userId => saveDraftData =>
  EmailService.saveDraft(userId, saveDraftData)
}

  // GET /emails/drafts/{draftId}
  val getDraftEndpoint = securedEndpoint.get
    .in("emails" / "drafts" / path[UUID]("draftId"))
    .out(jsonBody[EmailDraft])

  val getDraftServerEndpoint = getDraftEndpoint.serverLogic { userId => draftId =>
  EmailService.getDraft(userId, draftId)
}

  // POST /emails/send/{draftId}
  val sendEmailEndpoint = securedEndpoint.post
    .in("emails" / "send" / path[UUID]("draftId"))
    .out(statusCode(StatusCode.Accepted))

  val sendEmailServerEndpoint = sendEmailEndpoint.serverLogic { userId => draftId =>
  EmailService.sendEmail(userId, draftId)
}

  // Combine all email endpoints
  val all: List[ZServerEndpoint[EmailEndpointsEnv, Any]] = List(
    saveDraftServerEndpoint,
    getDraftServerEndpoint,
    sendEmailServerEndpoint
  )

end EmailEndpoints