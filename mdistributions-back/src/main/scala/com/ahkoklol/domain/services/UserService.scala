package com.ahkoklol.domain.services

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.*
import com.ahkoklol.domain.ports.UserRepository
import com.ahkoklol.infra.utils.JwtUtility
import zio.{IO, ZIO, ZLayer}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID

trait UserService:
  def register(register: User.Register): IO[AppError, User]
  def login(login: User.Login): IO[AppError, (User, String)] // Returns (User, JWT Token)
  def findById(id: UUID): IO[AppError, User]
  def update(id: UUID, update: User.Update): IO[AppError, Unit]
  def delete(id: UUID): IO[AppError, Unit]

object UserService:
  def register(register: User.Register): ZIO[UserService, AppError, User] =
    ZIO.serviceWithZIO[UserService](_.register(register))

  def login(login: User.Login): ZIO[UserService, AppError, (User, String)] =
    ZIO.serviceWithZIO[UserService](_.login(login))
  
  def findById(id: UUID): ZIO[UserService, AppError, User] =
    ZIO.serviceWithZIO[UserService](_.findById(id))

  def update(id: UUID, update: User.Update): ZIO[UserService, AppError, Unit] =
    ZIO.serviceWithZIO[UserService](_.update(id, update))

  def delete(id: UUID): ZIO[UserService, AppError, Unit] =
    ZIO.serviceWithZIO[UserService](_.delete(id))

case class UserServiceLive(repo: UserRepository, jwt: JwtUtility, encoder: BCryptPasswordEncoder) extends UserService:
  def register(register: User.Register): IO[AppError, User] =
    repo.findByEmail(register.email).foldZIO(
      {
        case AppError.UserNotFound(_) => // This is the expected "good" error
          val hashedPass = encoder.encode(register.password)
          repo.add(register.email, hashedPass)
        case otherError => ZIO.fail(otherError) // Propagate other errors
      },
      _ => ZIO.fail(AppError.EmailAlreadyExists()) // User already exists
    )

  def login(login: User.Login): IO[AppError, (User, String)] =
    for
      (user, hashedPass) <- repo.findByEmail(login.email)
      isValid           <- ZIO.succeed(encoder.matches(login.password, hashedPass))
      _                 <- ZIO.unless(isValid)(ZIO.fail(AppError.InvalidCredentials()))
      token             <- jwt.encode(user.id)
    yield (user, token)

  def findById(id: UUID): IO[AppError, User] = repo.findById(id)
  def update(id: UUID, update: User.Update): IO[AppError, Unit] = repo.update(id, update)
  def delete(id: UUID): IO[AppError, Unit] = repo.delete(id)

object UserServiceLive:
  val layer: ZLayer[UserRepository & JwtUtility, Nothing, UserService] =
    ZLayer.succeed(new BCryptPasswordEncoder()) >>>
      ZLayer.fromFunction(UserServiceLive.apply)