package com.ahkoklol.domain.services

import com.ahkoklol.domain.models.EmailDraft.{Save}
import com.ahkoklol.domain.models.EmailDraft
import com.ahkoklol.domain.models.User
import com.ahkoklol.domain.ports.{EmailRepository, GoogleSheetsClient, GoogleSmtpClient}
import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.errors.AppError.{DraftNotFoundError, EmailSendError, SheetsLinkFetchError, UserNotFoundError}
import zio.{IO, Task, ZIO, ZLayer}

import java.util.UUID

// Assuming these ports exist as ZIO services in the environment
trait EmailRepository:
  def save(draft: EmailDraft): IO[AppError, EmailDraft]
  def findByIdAndUser(id: UUID, userId: UUID): IO[AppError, Option[EmailDraft]]

trait GoogleSheetsClient:
  // Fetches a list of recipient emails from a given sheet URL and returns them
  def getRecipients(sheetsUrl: String): IO[AppError, List[String]]

trait GoogleSmtpClient:
  // Sends an email to a list of recipients using the user's credentials
  def send(recipient: String, subject: String, body: String, user: User): IO[AppError, Unit]


trait EmailService:
  /** Saves a new email draft or updates an existing one for the given user. */
  def saveDraft(userId: UUID, data: Save): IO[AppError, EmailDraft]

  /** Retrieves an email draft by ID for the given user. */
  def getDraft(id: UUID, userId: UUID): IO[AppError, EmailDraft]
  
  /** Fetches recipients, then iterates and sends the email to all of them. */
  def sendEmail(draftId: UUID, user: User): IO[AppError, Unit]


object EmailService:
  val live = ZLayer.fromFunction { (
    repo: EmailRepository, 
    sheetsClient: GoogleSheetsClient, 
    smtpClient: GoogleSmtpClient
  ) =>
    new EmailService {
      
      override def saveDraft(userId: UUID, data: Save): IO[AppError, EmailDraft] =
        val newDraft = EmailDraft(
          id = UUID.randomUUID(),
          userId = userId,
          googleSheetsLink = data.googleSheetsLink,
          subject = data.subject,
          body = data.body
        )
        repo.save(newDraft)

      override def getDraft(id: UUID, userId: UUID): IO[AppError, EmailDraft] =
        repo
          .findByIdAndUser(id, userId)
          .flatMap(ZIO.fromOption(_).orElseFail(DraftNotFoundError(id)))

      override def sendEmail(draftId: UUID, user: User): IO[AppError, Unit] =
        for {
          // 1. Retrieve the email draft
          draft <- getDraft(draftId, user.id)
          
          // 2. Fetch all recipient emails from Google Sheets
          recipients <- sheetsClient.getRecipients(draft.googleSheetsLink)
          
          // 3. Send email to each recipient
          _ <- ZIO.foreachParDiscard(recipients) { recipient =>
            smtpClient.send(recipient, draft.subject, draft.body, user).tapError { err =>
              // Log the error but continue trying other recipients
              ZIO.logError(s"Failed to send email to $recipient for draft $draftId: ${err.getMessage}")
            }.orElseFail(EmailSendError(s"Failed to send one or more emails. Check logs."))
          }
        } yield ()
    }
  }