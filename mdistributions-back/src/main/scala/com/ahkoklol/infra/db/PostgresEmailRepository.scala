package com.ahkoklol.infrastructure.db

import com.ahkoklol.domain.models.EmailDraft
import com.ahkoklol.domain.ports.EmailRepository
import com.ahkoklol.domain.errors.AppError
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.Read
import zio.interop.catz.*
import zio.{IO, ZLayer}

import java.util.UUID

final case class PostgresEmailRepository(xa: HikariTransactor[Task]) extends EmailRepository:
  
  // Implicit Read for mapping database rows back to the EmailDraft model
  private given Read[EmailDraft] = 
    Read[(UUID, UUID, String, String, String)].map { 
      case (id, userId, sheetsLink, subject, body) => 
        EmailDraft(id, userId, sheetsLink, subject, body) 
    }
  
  // --- SQL Queries ---
  private val insertSql = 
    sql"INSERT INTO email_drafts (id, user_id, sheets_link, subject, body) VALUES (?, ?, ?, ?, ?)"

  // --- Implementations ---
  override def save(draft: EmailDraft): IO[AppError, EmailDraft] =
    insertSql.toUpdate.run(draft.id, draft.userId, draft.googleSheetsLink, draft.subject, draft.body)
      .transact(xa)
      .map(_ => draft)
      .refineToOrDie[AppError]

  override def findByIdAndUser(id: UUID, userId: UUID): IO[AppError, Option[EmailDraft]] =
    sql"SELECT id, user_id, sheets_link, subject, body FROM email_drafts WHERE id = $id AND user_id = $userId"
      .query[EmailDraft]
      .option
      .transact(xa)
      .refineToOrDie[AppError]

object PostgresEmailRepository:
  val live: ZLayer[DoobieTransactor.Transactor, Nothing, EmailRepository] = 
    ZLayer.fromFunction(PostgresEmailRepository(_))