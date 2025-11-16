package com.example.back.app

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory

import dev.cheleb.ziotapir.laminar.*

import com.example.back.domain.*

given Form[secrets.Password] = secretForm(secrets.Password(_))

given f: WidgetFactory = UI5WidgetFactory

given session: Session[UserToken] = SessionLive[UserToken]
