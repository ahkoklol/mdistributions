package com.example.back.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import java.time.ZonedDateTime

case class User(
  id: Long,
  name: String,
  email: String,
  googleSheetsLink: Option[String],
  hashedPassword: String,
  creationDate: ZonedDateTime
) derives JsonCodec,
      Schema

case class UserID(id: Long, email: String) derives JsonCodec, Schema
