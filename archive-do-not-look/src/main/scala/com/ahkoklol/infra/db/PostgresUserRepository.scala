package com.ahkoklol.infra.db

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.*
import com.ahkoklol.domain.ports.UserRepository
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*
import doobie.implicits.*
import java.util.UUID

case class PostgresUserRepository(xa: Transactor[Task]) extends UserRepository:
  def findById(id: UUID): IO[AppError, User] = ??? // TODO: Implement Doobie query
  def findByEmail(email: String): IO[AppError, (User, String)] = ??? // TODO: Implement Doobie query
  def add(email: String, hashedPassword: String): IO[AppError, User] = ??? // TODO: Implement Doobie query
  def update(id: UUID, update: User.Update): IO[AppError, Unit] = ??? // TODO: Implement Doobie query
  def delete(id: UUID): IO[AppError, Unit] = ??? // TODO: Implement Doobie query

object PostgresUserRepository:
  val live: ZLayer[Transactor[Task], Nothing, UserRepository] =
    ZLayer.fromFunction(PostgresUserRepository.apply)