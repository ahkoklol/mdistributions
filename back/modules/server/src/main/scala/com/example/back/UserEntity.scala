package com.example.back

import java.time.ZonedDateTime
import com.example.back.domain.*
import io.scalaland.chimney.Transformer

case class NewUserEntity(
  id: Option[Long],
  name: String,
  email: String,
  googleSheetsLink: Option[String],
  hashedPassword: String,
  creationDate: ZonedDateTime
)

case class UserEntity(
  id: Long,
  name: String,
  email: String,
  googleSheetsLink: Option[String],
  hashedPassword: String,
  creationDate: ZonedDateTime
)

object UserEntity:
  given Transformer[UserEntity, User] = Transformer.derive
