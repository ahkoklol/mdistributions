package com.example.back.domain

import zio.json.JsonCodec
import sttp.tapir.Schema

case class Email(
  id: Option[Long] = None,
  userId: Long,
  subject: String,
  body: String,
  googleSheetsLink: String
) derives JsonCodec,
      Schema
