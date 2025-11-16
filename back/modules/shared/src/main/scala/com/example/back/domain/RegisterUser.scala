package com.example.back.domain

import zio.json.JsonCodec
import sttp.tapir.Schema

case class RegisterUser(
  name: String,
  email: String,
  password: String,
  passwordConfirmation: String,
  googleSheetsLink: Option[String] = None
) derives JsonCodec, Schema
