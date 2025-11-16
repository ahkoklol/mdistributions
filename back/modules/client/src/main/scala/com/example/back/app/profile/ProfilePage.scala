package com.example.back.app.profile

import com.raquo.laminar.api.L.*

import com.example.back.app.given
import com.example.back.domain.*
import com.example.back.http.endpoints.UserEndpoint

import dev.cheleb.ziotapir.laminar.*

object ProfilePage:

  val userBus = new EventBus[(User)]

  def apply() = div(
    child <-- session:
      // If the user is not logged in, show a message
      div(h1("Please log in to view your profile"))
      // If the user is logged in, show the profile page
    (_ =>
      div(
        onMountCallback { _ =>
          UserEndpoint.profile(()).emitTo(userBus)
        },
        div(
          h1("Profile Page"),
          child <-- userBus.events.map { user =>
            div(
              cls := "srf-form",
              h2("User"),
              div("Name: ", user.name),
              div("Email: ", user.email)
            )
          }
        )
      )
    )
  )
