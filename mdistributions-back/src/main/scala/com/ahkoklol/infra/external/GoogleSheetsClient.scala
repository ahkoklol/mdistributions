package com.ahkoklol.infrastructure.external

import com.ahkoklol.domain.ports.GoogleSheetsClient
import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.errors.AppError.SheetsLinkFetchError
import zio.{IO, ZIO, ZLayer}

object GoogleSheetsClientLive:
  val live: ZLayer[Any, Nothing, GoogleSheetsClient] = ZLayer.succeed {
    new GoogleSheetsClient:
      override def getRecipients(sheetsUrl: String): IO[AppError, List[String]] =
        ZIO.logInfo(s"Attempting to fetch recipients from sheets: $sheetsUrl") *>
        // In a real application, this is where you would call the Google Sheets API 
        // using the sheetsUrl to fetch a column of emails.
        ZIO.attempt {
          if sheetsUrl.contains("valid-sheet-id") then
            List("customer1@example.com", "customer2@example.com", "customer3@example.com")
          else
            throw new Exception("Invalid Google Sheet link or access denied.")
        }.refineOrDie {
          case e: Throwable => SheetsLinkFetchError(s"Google Sheets API failed: ${e.getMessage}")
        }
  }