package com.ahkoklol.infra.external

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.ports.{GoogleSheetsClient, GoogleSmtpClient}
import zio.{IO, ZIO, ZLayer}

// Mock implementation of GoogleSheetsClient
case class GoogleSheetsClientLive() extends GoogleSheetsClient:
  def getEmails(sheetsLink: String): IO[AppError, List[String]] =
    ZIO.succeed(List("customer1@example.com", "customer2@example.com")) // TODO: Implement real client

object GoogleSheetsClientLive:
  val layer: ZLayer[Any, Nothing, GoogleSheetsClient] =
    ZLayer.succeed(GoogleSheetsClientLive())

// Mock implementation of GoogleSmtpClient
case class GoogleSmtpClientLive() extends GoogleSmtpClient:
  def sendEmail(to: String, subject: String, body: String): IO[AppError, Unit] =
    ZIO.logInfo(s"Sending email to $to: $subject") // TODO: Implement real client

object GoogleSmtpClientLive:
  val layer: ZLayer[Any, Nothing, GoogleSmtpClient] =
    ZLayer.succeed(GoogleSmtpClientLive())
