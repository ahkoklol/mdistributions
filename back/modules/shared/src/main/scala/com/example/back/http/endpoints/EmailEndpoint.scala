package com.example.back.http.endpoints

import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.example.back.domain.*
import com.example.back.login.LoginPassword
import sttp.tapir.EndpointIO.Example

object EmailEndpoint extends BaseEndpoint:

  val createEmail: PublicEndpoint[Email, Throwable, Email, Any] = baseEndpoint
    .tag("email")
    .name("email")
    .post
    .in("email")
    .in(
      jsonBody[Email]
        .description("Email to send")
        .example(
          Email(
            1,
            42,
            "Welcome to our service",
            "Hello, thank you for joining us!",
            "https://sheet.link/to/recipients",
          )
        )
    )
    .out(jsonBody[Email])
    .description("Send email")

val getEmailById: PublicEndpoint[Long, Throwable, Option[Email], Any] = baseEndpoint
    .tag("email")
    .name("getById")
    .get
    .in("email" / path[Long]("id"))
    .out(jsonBody[Option[Email]])
    .description("Get email by ID")

val getEmails: PublicEndpoint[Unit, Throwable, List[Email], Any] = baseEndpoint
    .tag("email")
    .name("getEmails")
    .get
    .in("emails")
    .out(jsonBody[List[Email]])
    .description("Get all emails sorted by creation date descending")