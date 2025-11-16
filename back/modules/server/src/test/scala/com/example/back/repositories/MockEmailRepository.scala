package com.example.back.repositories

import zio.*
import com.example.back.*
import java.time.ZonedDateTime

class MockEmailRepository extends EmailRepository {
  private var store = Map.empty[Long, EmailEntity]
  private var counter = 0L

  override def create(email: EmailEntity): Task[EmailEntity] =
    ZIO.succeed {
      counter += 1
      val saved = email.copy(id = counter)
      store += (counter -> saved)
      saved
    }

  override def getById(id: Long): Task[Option[EmailEntity]] =
    ZIO.succeed(store.get(id))

  override def getByCreationDateDesc(): Task[List[EmailEntity]] =
    ZIO.succeed(store.values.toList.sortBy(_.creationDate)(Ordering[ZonedDateTime].reverse))
}