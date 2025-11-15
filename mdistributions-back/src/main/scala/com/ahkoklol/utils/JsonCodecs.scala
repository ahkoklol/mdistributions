package com.ahkoklol.utils

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.*
import com.ahkoklol.infra.utils.JwtClaim
import zio.json.*

import java.util.UUID

// Centralized JSON codecs for domain models
object JsonCodecs:

  // AppError Codecs
  case class ErrorResponse(error: String, message: String)
  object ErrorResponse:
    given encoder: JsonEncoder[ErrorResponse] = DeriveJsonEncoder.gen[ErrorResponse]
    given decoder: JsonDecoder[ErrorResponse] = DeriveJsonDecoder.gen[ErrorResponse]

  given appErrorEncoder: JsonEncoder[AppError] = ErrorResponse.encoder.contramap {
    case AppError.UserNotFound(msg)      => ErrorResponse("UserNotFound", msg)
    case AppError.EmailDraftNotFound(msg) => ErrorResponse("EmailDraftNotFound", msg)
    case AppError.Unauthorized(msg)      => ErrorResponse("Unauthorized", msg)
    case AppError.EmailAlreadyExists(msg) => ErrorResponse("EmailAlreadyExists", msg)
    case AppError.InvalidCredentials(msg) => ErrorResponse("InvalidCredentials", msg)
    case AppError.DatabaseError(msg)     => ErrorResponse("DatabaseError", msg)
    case AppError.UnknownError(msg)      => ErrorResponse("UnknownError", msg)
  }
  // We don't typically need a decoder for AppError

  // UUID Codecs
  given uuidEncoder: JsonEncoder[UUID] = JsonEncoder.string.contramap(_.toString)
  given uuidDecoder: JsonDecoder[UUID] = JsonDecoder.string.map(UUID.fromString)

  // User Model Codecs
  given userRegisterEncoder: JsonEncoder[User.Register] = DeriveJsonEncoder.gen[User.Register]
  given userRegisterDecoder: JsonDecoder[User.Register] = DeriveJsonDecoder.gen[User.Register]

  given userLoginEncoder: JsonEncoder[User.Login] = DeriveJsonEncoder.gen[User.Login]
  given userLoginDecoder: JsonDecoder[User.Login] = DeriveJsonDecoder.gen[User.Login]

  given userUpdateEncoder: JsonEncoder[User.Update] = DeriveJsonEncoder.gen[User.Update]
  given userUpdateDecoder: JsonDecoder[User.Update] = DeriveJsonDecoder.gen[User.Update]

  given userEncoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given userDecoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]

  // EmailDraft Model Codecs
  given emailDraftSaveEncoder: JsonEncoder[EmailDraft.Save] = DeriveJsonEncoder.gen[EmailDraft.Save]
  given emailDraftSaveDecoder: JsonDecoder[EmailDraft.Save] = DeriveJsonDecoder.gen[EmailDraft.Save]

  given emailDraftEncoder: JsonEncoder[EmailDraft] = DeriveJsonEncoder.gen[EmailDraft]
  given emailDraftDecoder: JsonDecoder[EmailDraft] = DeriveJsonDecoder.gen[EmailDraft]

  // JWT Claim Codec
  given jwtClaimEncoder: JsonEncoder[JwtClaim] = DeriveJsonEncoder.gen[JwtClaim]
  given jwtClaimDecoder: JsonDecoder[JwtClaim] = DeriveJsonDecoder.gen[JwtClaim]

end JsonCodecs