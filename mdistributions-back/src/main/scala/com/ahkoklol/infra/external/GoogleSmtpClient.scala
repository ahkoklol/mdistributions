package com.ahkoklol.infrastructure.external

import com.ahkoklol.domain.ports.GoogleSmtpClient
import com.ahkoklol.domain.models.User
import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.errors.AppError.EmailSendError
import zio.{IO, ZIO, ZLayer}
import jakarta.mail.internet.{MimeMessage, InternetAddress}
import jakarta.mail.{Session, Transport, Message}
import java.util.Properties

object GoogleSmtpClientLive:
  val live: ZLayer[Any, Nothing, GoogleSmtpClient] = ZLayer.succeed {
    new GoogleSmtpClient:
      override def send(recipient: String, subject: String, body: String, user: User): IO[AppError, Unit] =
        ZIO.logInfo(s"Attempting to send email to $recipient (User ID: ${user.id})") *>
        ZIO.attempt {
          // Check if the user has credentials (a prerequisite for sending)
          val credentials = user.googleSmtpCredential.getOrElse(
            throw new Exception("User has no Google SMTP credentials configured.")
          )
          
          // --- Simplified SMTP Logic Placeholder ---
          val props = new Properties()
          props.put("mail.smtp.auth", "true")
          props.put("mail.smtp.starttls.enable", "true")
          props.put("mail.smtp.host", "smtp.gmail.com")
          props.put("mail.smtp.port", "587")

          // Note: Actual implementation would use the user's OAuth/Service Account credentials (e.g., in `credentials`)
          // for the `Session` and `Transport` classes.
          
          val session = Session.getInstance(props) // Simplified session creation
          val message = new MimeMessage(session)
          
          message.setFrom(new InternetAddress(user.email)) // Sender email (must match authenticated account)
          message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient))
          message.setSubject(subject)
          message.setText(body)

          // Transport.send(message) // <-- This is the actual synchronous send call

          ZIO.logInfo(s"Simulated send successful to $recipient using credentials: $credentials")
          ()
        }.refineOrDie {
          case e: Throwable => EmailSendError(s"SMTP send failed for ${recipient}: ${e.getMessage}")
        }
  }