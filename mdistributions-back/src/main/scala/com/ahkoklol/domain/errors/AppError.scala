package com.ahkoklol.domain.errors

sealed trait AppError extends Throwable

object AppError:
  // User Errors
  final case class UserAlreadyExists(email: String) extends AppError
  final case class UserNotFoundError(id: java.util.UUID) extends AppError
  final case class AuthenticationError(message: String) extends AppError
  final case class InvalidUpdateData(message: String) extends AppError
  
  // Email Errors
  final case class DraftNotFoundError(id: java.util.UUID) extends AppError
  final case class SheetsLinkFetchError(message: String) extends AppError
  final case class EmailSendError(message: String) extends AppError