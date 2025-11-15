package com.ahkoklol.domain.models

import java.util.UUID

final case class User(
    id: UUID,
    email: String,
    passwordHash: String, // Storing password hash, not plaintext
    // For connecting to Google APIs for sending emails and fetching sheets
    googleSmtpCredential: Option[String], // e.g., a service account key or token
    googleSheetsLink: Option[String]      // The user's main Google Sheet link to retrieve customers
)

object User:
  // --- DTOs for Common Operations ---

  // Used for user registration
  final case class Register(
      email: String,
      password: String
  )

  // Used for user login
  final case class Login(
      email: String,
      password: String
  )

  // Used for updating user profile or settings
  final case class Update(
      newEmail: Option[String] = None,
      newPassword: Option[String] = None,
      newGoogleSmtpCredential: Option[String] = None,
      newGoogleSheetsLink: Option[String] = None
  )