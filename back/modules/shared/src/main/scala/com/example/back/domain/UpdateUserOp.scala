package com.example.back.domain

import zio.json.JsonCodec
import sttp.tapir.Schema

case class UpdateUserOp(
  name: Option[String] = None,
  googleSheetsLink: Option[String] = None
) derives JsonCodec, Schema