package com.ahkoklol.domain.ports

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.*
import zio.{IO, ZIO}
import java.util.UUID

trait UserRepository:
  def findById(id: UUID): IO[AppError, User]
  def findByEmail(email: String): IO[AppError, (User, String)] // Returns (User, HashedPassword)
  def add(email: String, hashedPassword: String): IO[AppError, User]
  def update(id: UUID, update: User.Update): IO[AppError, Unit]
  def delete(id: UUID): IO[AppError, Unit]

trait EmailRepository:
  def saveDraft(userId: UUID, draft: EmailDraft.Save): IO[AppError, EmailDraft]
  def getDraft(userId: UUID, draftId: UUID): IO[AppError, EmailDraft]