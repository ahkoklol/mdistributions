package com.example.back.repositories

import zio.*

import com.example.back.EmailEntity

import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres

trait EmailRepository {
    def create(email: EmailEntity): Task[EmailEntity]
    def getById(id: Long): Task[Option[EmailEntity]]
    def getByCreationDateDesc(): Task[List[EmailEntity]]
}

class EmailRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends EmailRepository {

  import quill.*

  inline given SchemaMeta[EmailEntity] = schemaMeta[EmailEntity]("emails")
  inline given InsertMeta[EmailEntity] = insertMeta[EmailEntity](_.id)
  inline given UpdateMeta[EmailEntity] = updateMeta[EmailEntity](_.id, _.creationDate)

  override def create(email: EmailEntity): Task[EmailEntity] =
    run(query[EmailEntity].insertValue(lift(email)).returning(r => r))

  override def getById(id: Long): Task[Option[EmailEntity]] =
    run(query[EmailEntity].filter(_.id == lift(id))).map(_.headOption)

  override def getByCreationDateDesc(): Task[List[EmailEntity]] =
    run(query[EmailEntity].sortBy(e => e.creationDate)(Ord.desc))
}

object EmailRepositoryLive {
  def layer: RLayer[Postgres[SnakeCase], EmailRepository] = ZLayer.derive[EmailRepositoryLive]
}
