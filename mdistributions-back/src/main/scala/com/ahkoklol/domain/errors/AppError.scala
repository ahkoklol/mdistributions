package com.ahkoklol.domain.errors

// Sealed trait for all application-specific errors
enum AppError(val message: String):
  case UserNotFound(msg: String = "User not found") extends AppError(msg)
  case EmailDraftNotFound(msg: String = "Email draft not found") extends AppError(msg)
  case Unauthorized(msg: String = "Unauthorized") extends AppError(msg)
  case EmailAlreadyExists(msg: String = "Email already exists") extends AppError(msg)
  case InvalidCredentials(msg: String = "Invalid email or password") extends AppError(msg)
  case DatabaseError(msg: String = "A database error occurred") extends AppError(msg)
  case UnknownError(msg: String = "An unknown error occurred") extends AppError(msg)

object AppError:
  // Helper to convert Throwables to AppError
  def fromThrowable(t: Throwable): AppError = t match
    case e: AppError => e
    case _           => DatabaseError(t.getMessage)