package com.ahkoklol.domain.ports

import com.ahkoklol.domain.errors.AppError
import zio.IO

trait GoogleSheetsClient:
  def getEmails(sheetsLink: String): IO[AppError, List[String]]

trait GoogleSmtpClient:
  def sendEmail(to: String, subject: String, body: String): IO[AppError, Unit]
