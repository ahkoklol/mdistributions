package com.ahkoklol.infrastructure.db

import com.ahkoklol.domain.models.User
import com.ahkoklol.domain.ports.UserRepository
import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.errors.AppError.UserNotFoundError
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.Read
import doobie.util.query.Query
import doobie.util.update.Update
import zio.interop.catz.*
import zio.{IO, ZIO, ZLayer}

import java.util.UUID

final case class PostgresUserRepository(xa: HikariTransactor[Task]) extends UserRepository:
  
  // Implicit Read for mapping database rows back to the User model
  private given Read[User] = 
    Read[(UUID, String, String, Option[String], Option[String])].map { 
      case (id, email, hash, smtpCred, sheetsLink) => 
        User(id, email, hash, smtpCred, sheetsLink) 
    }

  // --- SQL Queries ---
  private def selectUser(whereSql: Fragment): Query0[User] = 
    (sql"SELECT id, email, password_hash, google_smtp_credential, google_sheets_link FROM users " ++ whereSql).query[User]

  private val insertSql = 
    sql"INSERT INTO users (id, email, password_hash) VALUES (?, ?, ?)"

  private val updateSql =
    sql"""
      UPDATE users SET 
        email = ?, 
        password_hash = ?, 
        google_smtp_credential = ?, 
        google_sheets_link = ? 
      WHERE id = ?
    """

  // --- Implementations ---
  override def findByEmail(email: String): IO[AppError, Option[User]] =
    selectUser(sql"WHERE email = $email").option.transact(xa).refineOrDie { 
      case e: Throwable => AppError.AuthenticationError(s"DB error: ${e.getMessage}") 
    }

  override def findById(id: UUID): IO[AppError, Option[User]] =
    selectUser(sql"WHERE id = $id").option.transact(xa).refineToOrDie[AppError]
    
  override def save(user: User): IO[AppError, User] =
    insertSql.toUpdate.run(user.id, user.email, user.passwordHash)
      .transact(xa)
      .map(_ => user)
      .refineToOrDie[AppError]

  override def update(user: User): IO[AppError, Unit] =
    updateSql.run(user.email, user.passwordHash, user.googleSmtpCredential, user.googleSheetsLink, user.id)
      .transact(xa)
      .flatMap(i => ZIO.fail(UserNotFoundError(user.id)).when(i == 0))
      .mapError(e => e.fold(_ => e, identity)) // Convert errors to AppError
      .unit

  override def delete(id: UUID): IO[AppError, Unit] =
    sql"DELETE FROM users WHERE id = $id".update.run
      .transact(xa)
      .unit
      .refineToOrDie[AppError]

object PostgresUserRepository:
  val live: ZLayer[DoobieTransactor.Transactor, Nothing, UserRepository] = 
    ZLayer.fromFunction(PostgresUserRepository(_))