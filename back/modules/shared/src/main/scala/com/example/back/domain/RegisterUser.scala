package com.example.back.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import zio.prelude.Debug
import zio.prelude.magnolia.*

case class RegisterUser(
  name: String,
  email: String,
  password: secrets.Password,
  passwordConfirmation: secrets.Password,
  googleSheetsLink: Option[String] = None
) derives JsonCodec,
      Schema,
      Debug:

  def errorMessages: Set[String] =
    var errors = Set.empty[String]

    if name.trim.isEmpty then errors += "Name cannot be empty"

    if !email.contains("@") || !email.contains(".") then errors += "Email is not valid"

    if password.value.length < 6 then errors += "Password must be at least 6 characters long"

    if password != passwordConfirmation then errors += "Password and confirmation do not match"

    errors

import zio.prelude.Debug.Repr
object secrets:

  opaque type Password <: String = String

  object Password:
    def apply(s: String): Password            = s
    extension (p: Password) def value: String = p

  given JsonCodec[Password] = JsonCodec.string
  given Schema[Password]    = Schema.string

  given Debug[Password] with
    def debug(value: Password): Repr = Repr.String("*****")
