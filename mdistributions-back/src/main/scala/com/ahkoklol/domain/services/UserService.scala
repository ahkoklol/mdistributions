package com.ahkoklol.domain.services

import com.ahkoklol.domain.models.User.{Login, Register, Update}
import com.ahkoklol.domain.models.User
import com.ahkoklol.domain.ports.UserRepository
import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.errors.AppError.{AuthenticationError, InvalidUpdateData, UserAlreadyExists, UserNotFoundError}
import zio.{IO, ZIO, ZLayer}

import java.util.UUID

// Assuming a HashUtility for password handling
trait HashUtility:
  def hash(plaintext: String): String
  def verify(plaintext: String, hash: String): Boolean

trait UserService:
  /** Registers a new user. */
  def register(data: Register): IO[AppError, User]
  
  /** Authenticates a user and returns their object. */
  def login(data: Login): IO[AppError, User]

  /** Retrieves a user by ID. */
  def getUser(id: UUID): IO[AppError, User]
  
  /** Updates a user's details. */
  def update(id: UUID, data: Update): IO[AppError, Unit]

  /** Deletes a user's account. */
  def delete(id: UUID): IO[AppError, Unit]


object UserService:
  // Live implementation of the service (Adapter)
  val live = ZLayer.fromFunction { (repo: UserRepository, hash: HashUtility) =>
    new UserService {
      
      override def register(data: Register): IO[AppError, User] =
        for {
          // 1. Check if user already exists
          existing <- repo.findByEmail(data.email)
          _ <- ZIO.fail(UserAlreadyExists(data.email)).when(existing.isDefined)
          
          // 2. Hash password and create new user object
          passwordHash = hash.hash(data.password)
          newUser = User(UUID.randomUUID(), data.email, passwordHash, None, None)
          
          // 3. Save to database
          user <- repo.save(newUser)
        } yield user

      override def login(data: Login): IO[AppError, User] =
        for {
          // 1. Find user by email
          userOption <- repo.findByEmail(data.email)
          user <- ZIO.fromOption(userOption).orElseFail(AuthenticationError("Invalid email or password"))
          
          // 2. Verify password
          isValid <- ZIO.succeed(hash.verify(data.password, user.passwordHash))
          _ <- ZIO.fail(AuthenticationError("Invalid email or password")).unless(isValid)
        } yield user

      override def getUser(id: UUID): IO[AppError, User] =
        repo.findById(id).flatMap(ZIO.fromOption(_).orElseFail(UserNotFoundError(id)))

      override def update(id: UUID, data: Update): IO[AppError, Unit] =
        for {
          // 1. Get existing user
          existingUser <- getUser(id)
          
          // 2. Construct updated user object
          newPasswordHash = data.newPassword.map(hash.hash).getOrElse(existingUser.passwordHash)
          updatedUser = existingUser.copy(
            email = data.newEmail.getOrElse(existingUser.email),
            passwordHash = newPasswordHash,
            googleSmtpCredential = data.newGoogleSmtpCredential.orElse(existingUser.googleSmtpCredential),
            googleSheetsLink = data.newGoogleSheetsLink.orElse(existingUser.googleSheetsLink)
          )
          
          // 3. Save update
          _ <- repo.update(updatedUser)
        } yield ()

      override def delete(id: UUID): IO[AppError, Unit] =
        repo.delete(id)
    }
  }