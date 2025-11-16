package com.ahkoklol.infra.db

import com.ahkoklol.domain.errors.AppError
import com.ahkoklol.domain.models.*
import com.ahkoklol.domain.ports.EmailRepository
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*
import java.util.UUID

case class PostgresEmailRepository(xa: Transactor[Task]) extends EmailRepository:
  def saveDraft(userId: UUID, draft: EmailDraft.Save): IO[AppError, EmailDraft] = ??? // TODO: Implement Doobie query
  def getDraft(userId: UUID, draftId: UUID): IO[AppError, EmailDraft] = ??? // TODO: Implement Doobie query

object PostgresEmailRepository:
  val live: ZLayer[Transactor[Task], Nothing, EmailRepository] =
    ZLayer.fromFunction(PostgresEmailRepository.apply)