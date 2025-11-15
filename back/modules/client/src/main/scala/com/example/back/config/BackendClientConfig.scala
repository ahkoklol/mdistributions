package com.example.back.config

import sttp.model.Uri

final case class BackendClientConfig(
  baseUrl: Option[Uri]
)
