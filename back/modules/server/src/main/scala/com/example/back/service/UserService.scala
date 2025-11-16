package com.example.back.service

import zio.*

import io.scalaland.chimney.dsl._
import java.time.ZonedDateTime

import com.example.back.domain.*
import com.example.back.domain.errors.*
import com.example.back.repositories.UserRepository
import com.example.back.UserEntity
import com.example.back.NewUserEntity
import com.example.back.repositories.TransactionSupport

import java.sql.SQLException

import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill.Postgres

trait UserService {
    def register(name: String, email: String, password: String, googleSheetsLink: Option[String]): Task[User]
    def login(email: String, password: String): Task[User]
    def getUserById(id: Long): Task[User]
    def findByEmail(email: String): Task[Option[User]]
    def updateUser(id: Long, op: User => User): Task[User]
    def deleteUser(id: Long): Task[User]
}

class UserServiceLive private (
  userRepository: UserRepository,
  quill: Quill.Postgres[SnakeCase]
) extends UserService
    with TransactionSupport(quill) {

  override def register(name: String, email: String, password: String, googleSheetsLink: Option[String]): Task[User] =
    tx(
      for {
        _    <- ZIO.logDebug(s"Registering user: $email")
        user <- userRepository
                  .create(
                    NewUserEntity(
                      None,
                      name = name,
                      email = email,
                      googleSheetsLink = googleSheetsLink,
                      hashedPassword = Hasher.generatedHash(password),
                      creationDate = ZonedDateTime.now()
                    )
                  )
                  .catchSome { case e: SQLException =>
                    ZIO.logError(s"Error code: ${e.getSQLState} while creating user: ${e.getMessage}")
                      *> ZIO.fail(UserAlreadyExistsException())
                  }
                  .mapInto[User]
      } yield user
    )

  override def login(email: String, password: String): Task[User] =
    userRepository
      .findByEmail(email)
      .map {
        _.filter(user => Hasher.validateHash(password, user.hashedPassword))
      }
      .someOrFail(InvalidCredentialsException())
      .mapInto[User]

  override def getUserById(id: Long): Task[User] =
    userRepository
      .getById(id)
      .someOrFail(UserNotFoundException(id.toString))
      .mapInto[User]

override def findByEmail(email: String): Task[Option[User]] =
  userRepository
    .findByEmail(email)          // Task[Option[UserEntity]]
    .map(_.map(_.into[User].transform))  // map Option inside Task

override def updateUser(id: Long, op: User => User): Task[User] =
  userRepository
    .update(
      id,
      userEntity => op(userEntity.into[User].transform).into[UserEntity].transform
    )
    .mapInto[User]

override def deleteUser(id: Long): Task[User] =
    userRepository
    .delete(id)
    .mapInto[User]
}

object UserServiceLive {
  val layer: RLayer[UserRepository & Postgres[SnakeCase], UserService] = ZLayer.derive[UserServiceLive]
}