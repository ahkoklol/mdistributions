package com.ahkoklol.infrastructure.utils

import com.ahkoklol.domain.services.HashUtility
import org.mindrot.jbcrypt.BCrypt // <-- NEW IMPORT
import zio.ZLayer

object BcryptHashUtility:
  val live: ZLayer[Any, Nothing, HashUtility] = ZLayer.succeed {
    new HashUtility:
      
      // Use BCrypt.hashpw with a generated salt
      override def hash(plaintext: String): String = 
        BCrypt.hashpw(plaintext, BCrypt.gensalt())

      // Use BCrypt.checkpw for verification
      override def verify(plaintext: String, hash: String): Boolean = 
        BCrypt.checkpw(plaintext, hash)
  }