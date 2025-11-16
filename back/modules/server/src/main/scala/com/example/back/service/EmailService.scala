package com.example.back.service

import zio.*

import io.scalaland.chimney.dsl._
import java.time.ZonedDateTime

import com.example.back.domain.*
import com.example.back.domain.errors.*
import com.example.back.repositories.EmailRepository
import com.example.back.EmailEntity
import com.example.back.repositories.TransactionSupport

import java.sql.SQLException

import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill.Postgres

trait EmailService {
    def create(email: EmailEntity): Task[EmailEntity]
    def getById(id: Long): Task[Option[EmailEntity]]
    def getByCreationDateDesc(): Task[List[EmailEntity]]
}

class EmailServiceLive private (
  emailRepository: EmailRepository,
  quill: Quill.Postgres[SnakeCase]
) extends EmailService
    with TransactionSupport(quill) {

  override def create(email: EmailEntity): Task[EmailEntity] =
    emailRepository.create(email)
    // add smtp sending here

  override def getById(id: Long): Task[Option[EmailEntity]] =
    emailRepository.getById(id)

  override def getByCreationDateDesc(): Task[List[EmailEntity]] =
    emailRepository.getByCreationDateDesc()
}
        
object EmailServiceLive {
  val layer: RLayer[EmailRepository & Postgres[SnakeCase], EmailService] = ZLayer.derive[EmailServiceLive]
}