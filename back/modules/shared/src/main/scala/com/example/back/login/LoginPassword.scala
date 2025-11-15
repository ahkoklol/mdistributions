package com.example.back.login

import sttp.tapir.Schema
import dev.cheleb.scalamigen.NoPanel
import com.example.back.domain.Password

@NoPanel
final case class LoginPassword(login: String, password: Password) derives zio.json.JsonCodec, Schema:
  def isIncomplete: Boolean = login.isBlank || password.isBlank
