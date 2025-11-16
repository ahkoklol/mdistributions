package com.example.back

import java.time.ZonedDateTime
import com.example.back.domain.*
import io.scalaland.chimney.Transformer

case class EmailEntity(
  id: Long,
  userId: Long,
  subject: String,
  body: String,
  googleSheetsLink: String, // not optional as emails can be sent to different groups
  creationDate: ZonedDateTime
)

object EmailEntity:
  given Transformer[EmailEntity, Email] = Transformer.derive