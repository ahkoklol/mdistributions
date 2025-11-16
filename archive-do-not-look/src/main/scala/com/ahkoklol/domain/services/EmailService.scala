package com.ahkoklol.domain.services

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.{EmailDraft, User}
import com.ahkoklol.domain.ports.{EmailRepository, GoogleSheetsClient, GoogleSmtpClient, UserRepository}
import zio.{IO, ZIO, ZLayer}
import java.util.UUID

trait EmailService:
  def saveDraft(userId: UUID, draft: EmailDraft.Save): IO[AppError, EmailDraft]
  def getDraft(userId: UUID, draftId: UUID): IO[AppError, EmailDraft]
  def sendEmail(userId: UUID, draftId: UUID): IO[AppError, Unit]

object EmailService:
  def saveDraft(userId: UUID, draft: EmailDraft.Save): ZIO[EmailService, AppError, EmailDraft] =
    ZIO.serviceWithZIO[EmailService](_.saveDraft(userId, draft))
  
  def getDraft(userId: UUID, draftId: UUID): ZIO[EmailService, AppError, EmailDraft] =
    ZIO.serviceWithZIO[EmailService](_.getDraft(userId, draftId))

  def sendEmail(userId: UUID, draftId: UUID): ZIO[EmailService, AppError, Unit] =
    ZIO.serviceWithZIO[EmailService](_.sendEmail(userId, draftId))

case class EmailServiceLive(
    userRepo: UserRepository,
    emailRepo: EmailRepository,
    sheetsClient: GoogleSheetsClient,
    smtpClient: GoogleSmtpClient
) extends EmailService:

  def saveDraft(userId: UUID, draft: EmailDraft.Save): IO[AppError, EmailDraft] =
    emailRepo.saveDraft(userId, draft)

  def getDraft(userId: UUID, draftId: UUID): IO[AppError, EmailDraft] =
    emailRepo.getDraft(userId, draftId)

  def sendEmail(userId: UUID, draftId: UUID): IO[AppError, Unit] =
    for
      user     <- userRepo.findById(userId)
      draft    <- emailRepo.getDraft(userId, draftId)
      sheetsLink <- ZIO.fromOption(user.googleSheetsLink).mapError(_ => AppError.UnknownError("User has no Google Sheets link"))
      emails   <- sheetsClient.getEmails(sheetsLink)
      _        <- ZIO.foreachPar(emails)(email => smtpClient.sendEmail(email, draft.subject, draft.body))
    yield ()

object EmailServiceLive:
  val layer: ZLayer[UserRepository & EmailRepository & GoogleSheetsClient & GoogleSmtpClient, Nothing, EmailService] =
    ZLayer.fromFunction(EmailServiceLive.apply)