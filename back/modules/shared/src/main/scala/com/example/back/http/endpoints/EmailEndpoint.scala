package com.example.back.http.endpoints

import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import com.example.back.domain.*

object EmailEndpoint extends BaseEndpoint:

    val createEmail: Endpoint[String, Email, Throwable, Email, Any] = baseSecuredEndpoint
    .tag("email")
    .name("email")
    .post
    .in("email")
    .in(
      jsonBody[Email]
        .description("Email to send")
        .example(
          Email(
            id = Some(1),
            userId = 42,
            subject = "Welcome to our service",
            body = "Hello, thank you for joining us!",
            googleSheetsLink = "https://sheet.link/to/recipients"
            )
        )
    )
    .out(jsonBody[Email])
    .description("Send email")

    val getEmailById: Endpoint[String, Long, Throwable, Option[Email], Any] = baseSecuredEndpoint
        .tag("email")
        .name("getById")
        .get
        .in("email" / path[Long]("id"))
        .out(jsonBody[Option[Email]])
        .description("Get email by ID")

    val getEmails: Endpoint[String, Unit, Throwable, List[Email], Any] = baseSecuredEndpoint
        .tag("email")
        .name("getEmails")
        .get
        .in("emails")
        .out(jsonBody[List[Email]])
        .description("Get all emails sorted by creation date descending")