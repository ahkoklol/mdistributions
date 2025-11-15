package com.ahkoklol.domain.models

import java.util.UUID

final case class EmailDraft(
    id: UUID,
    userId: UUID,               // Foreign key linking to the User who owns this draft
    googleSheetsLink: String,   // The specific Google Sheets URL for this email campaign
    subject: String,
    body: String
)

object EmailDraft:
  // Used for saving a new draft or updating an existing one.
  // Note: userId will be implicitly derived from the authenticated user context.
  final case class Save(
      googleSheetsLink: String,
      subject: String,
      body: String
  )