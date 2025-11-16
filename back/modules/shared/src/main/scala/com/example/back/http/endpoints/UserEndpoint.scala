package com.example.back.http.endpoints

import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.example.back.domain.*
import com.example.back.login.LoginPassword
import sttp.tapir.EndpointIO.Example

object UserEndpoint extends BaseEndpoint:

  val register: PublicEndpoint[RegisterUser, Throwable, User, Any] = baseEndpoint
    .tag("user")
    .name("register")
    .post
    .in("user/register")
    .in(
      jsonBody[RegisterUser]
        .description("User to register")
        .example(
          RegisterUser(
            name = "John",
            email = "john.doe@foo.bar",
            password = "notsecured",
            passwordConfirmation = "notsecured",
            googleSheetsLink = Some("https://sheet.link")
            )
        )
    )
    .out(jsonBody[User])
    .description("Register user")

val login: PublicEndpoint[LoginPassword, Throwable, UserToken, Any] = baseEndpoint
    .tag("user")
    .name("login")
    .post
    .in("user/login")
    .in(
        jsonBody[LoginPassword]
    )
    .out(jsonBody[UserToken])
    .description("Login")

val profile: Endpoint[String, Throwable, User, Any] = baseSecuredEndpoint
    .tag("user")
    .name("profile")
    .get
    .in("user/profile")
    .out(jsonBody[User])
    .description("Get user profile")

val update: Endpoint[String, UpdateUserOp, Throwable, User, Any] = baseSecuredEndpoint
    .tag("user")
    .name("update")
    .put
    .in("user/update")
    .in(
        jsonBody[UpdateUserOp]
    )
    .out(jsonBody[User])
    .description("Update user")

val delete: Endpoint[String, Throwable, User, Any] = baseSecuredEndpoint
    .tag("user")
    .name("delete")
    .delete
    .in("user/delete")
    .out(jsonBody[User])
    .description("Delete user")
