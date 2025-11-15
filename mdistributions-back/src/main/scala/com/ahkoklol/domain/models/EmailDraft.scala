package com.ahkoklol.domain.models

import java.util.UUID

// Final EmailDraft model
case class EmailDraft(
    id: UUID,
    userId: UUID,
    subject: String,
    body: String
)

object EmailDraft:
  // Model for saving a draft
  case class Save(
      subject: String,
      body: String
  )