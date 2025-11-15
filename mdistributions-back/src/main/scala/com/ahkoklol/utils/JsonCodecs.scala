package com.ahkoklol.utils

import com.ahkoklol.domain.models.User
import com.ahkoklol.domain.models.User.{Login, Register, Update}
import com.ahkoklol.domain.models.EmailDraft.Save
import com.ahkoklol.domain.models.EmailDraft
import com.ahkoklol.domain.errors.AppError
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import java.util.UUID

// Define codecs for basic types needed by models
object JsonCodecs:

  // Standard UUID codecs
  given uuidEncoder: JsonEncoder[UUID] = JsonEncoder[String].contramap(_.toString)
  given uuidDecoder: JsonDecoder[UUID] = JsonDecoder[String].map(UUID.fromString)

  // --- User Codecs ---

  given userEncoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given userDecoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]

  given registerEncoder: JsonEncoder[Register] = DeriveJsonEncoder.gen[Register]
  given registerDecoder: JsonDecoder[Register] = DeriveJsonDecoder.gen[Register]

  given loginEncoder: JsonEncoder[Login] = DeriveJsonEncoder.gen[Login]
  given loginDecoder: JsonDecoder[Login] = DeriveJsonDecoder.gen[Login]

  given updateEncoder: JsonEncoder[Update] = DeriveJsonEncoder.gen[Update]
  given updateDecoder: JsonDecoder[Update] = DeriveJsonDecoder.gen[Update]

  // --- EmailDraft Codecs ---

  given emailDraftEncoder: JsonEncoder[EmailDraft] = DeriveJsonEncoder.gen[EmailDraft]
  given emailDraftDecoder: JsonDecoder[EmailDraft] = DeriveJsonDecoder.gen[EmailDraft]

  given saveDraftEncoder: JsonEncoder[Save] = DeriveJsonEncoder.gen[Save]
  given saveDraftDecoder: JsonDecoder[Save] = DeriveJsonDecoder.gen[Save]
  
  // --- Error Codecs (for Tapir error handling) ---
  
  final case class ErrorResponse(reason: String)
  given errorResponseEncoder: JsonEncoder[ErrorResponse] = DeriveJsonEncoder.gen[ErrorResponse]
  given errorResponseDecoder: JsonDecoder[ErrorResponse] = DeriveJsonDecoder.gen[ErrorResponse]
  
  // Custom codec to map AppError to a common HTTP response body
  given appErrorEncoder: JsonEncoder[AppError] = ErrorResponse.encoder.contramap(err =>
    ErrorResponse(s"${err.getClass.getSimpleName}: ${err.getMessage}")
  )
