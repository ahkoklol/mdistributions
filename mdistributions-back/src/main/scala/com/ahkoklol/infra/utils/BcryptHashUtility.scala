package com.ahkoklol.infrastructure.utils

import com.ahkoklol.domain.services.HashUtility
import com.github.t3hnar.bcrypt.Password
import zio.ZLayer

object BcryptHashUtility:
  val live: ZLayer[Any, Nothing, HashUtility] = ZLayer.succeed {
    new HashUtility:
      // Uses a default salt log rounds (12 is a common secure default)
      override def hash(plaintext: String): String = 
        plaintext.bcrypt

      override def verify(plaintext: String, hash: String): Boolean = 
        plaintext.isBcrypted(hash)
  }