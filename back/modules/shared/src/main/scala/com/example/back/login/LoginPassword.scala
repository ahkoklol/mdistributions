package com.example.back.login

import sttp.tapir.Schema
import dev.cheleb.scalamigen.NoPanel

@NoPanel
final case class LoginPassword(
  login: String,
  password: String
) derives zio.json.JsonCodec, Schema:
  def isIncomplete: Boolean = login.isBlank || password.isBlank
