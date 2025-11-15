package com.ahkoklol.domain.ports

import com.ahkoklol.domain.models.User
import com.ahkoklol.domain.errors.AppError
import zio.{IO, ULayer, ZIO, ZLayer}

import java.util.UUID

trait UserRepository:
  def findByEmail(email: String): IO[AppError, Option[User]]
  def findById(id: UUID): IO[AppError, Option[User]]
  def save(user: User): IO[AppError, User]
  def update(user: User): IO[AppError, Unit]
  def delete(id: UUID): IO[AppError, Unit]

object UserRepository:
  // Accessor methods for ZIO environment
  def findByEmail(email: String): ZIO[UserRepository, AppError, Option[User]] =
    ZIO.serviceWithZIO(_.findByEmail(email))

// ... other repository traits (EmailRepository, GoogleSheetsClient, GoogleSmtpClient) would go here