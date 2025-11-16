package com.example.back.repositories

import zio.*
import com.example.back.*

class MockUserRepository extends UserRepository {
  private var store = Map.empty[Long, UserEntity]
  private var counter = 0L

  override def create(user: NewUserEntity): Task[UserEntity] =
    ZIO.succeed {
      counter += 1
      val saved = UserEntity(
        id = counter,
        name = user.name,
        email = user.email,
        googleSheetsLink = user.googleSheetsLink,
        hashedPassword = user.hashedPassword,
        creationDate = user.creationDate
      )
      store += (counter -> saved)
      saved
    }

  override def getById(id: Long): Task[Option[UserEntity]] =
    ZIO.succeed(store.get(id))

  override def findByEmail(email: String): Task[Option[UserEntity]] =
    ZIO.succeed(store.values.find(_.email == email))

  override def update(id: Long, f: UserEntity => UserEntity): Task[UserEntity] =
    store.get(id) match {
      case Some(user) =>
        val updated = f(user)
        store += (id -> updated)
        ZIO.succeed(updated)
      case None => ZIO.fail(new RuntimeException("User not found"))
    }

  override def delete(id: Long): Task[UserEntity] =
    store.get(id) match {
      case Some(user) =>
        store -= id
        ZIO.succeed(user)
      case None => ZIO.fail(new RuntimeException("User not found"))
    }
}
