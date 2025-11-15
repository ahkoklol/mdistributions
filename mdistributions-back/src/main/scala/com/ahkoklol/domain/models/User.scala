package com.ahkoklol.domain.models

import java.util.UUID

// Final User model stored in DB and returned to client
case class User(
    id: UUID,
    email: String,
    googleSheetsLink: Option[String]
)

object User:
  // Model for user registration
  case class Register(
      email: String,
      password: String // Plain text, will be hashed in service layer
  )

  // Model for user login
  case class Login(
      email: String,
      password: String
  )

  // Model for updating user info
  case class Update(
      googleSheetsLink: Option[String]
  )