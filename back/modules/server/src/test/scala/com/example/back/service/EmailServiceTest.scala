import zio.*
import zio.test.*
import zio.test.Assertion.*
import java.time.ZonedDateTime
import com.example.back.*
import com.example.back.service.*
import com.example.back.repositories.*
import io.getquill.SnakeCase

object EmailServiceSpec extends ZIOSpecDefault {

  val mockRepoLayer = ZLayer.succeed(new MockEmailRepository)

  // Use your Postgres testcontainer layer
  val testLayer = mockRepoLayer ++ PostgresTestcontainer.live >>> EmailServiceLive.layer

  def spec = suite("EmailService")(
    test("create and fetch email") {
      val email = EmailEntity(0, 1, "Hello", "Body", "sheet-link", ZonedDateTime.now())
      val program = for {
        service <- ZIO.service[EmailService]
        created <- service.create(email)
        fetched <- service.getById(created.id)
      } yield assert(fetched)(isSome(equalTo(created)))

      program.provideLayer(testLayer)
    },
    test("get emails in descending creation date") {
      val email1 = EmailEntity(0, 1, "s1", "b1", "sheet1", ZonedDateTime.now())
      val email2 = EmailEntity(0, 2, "s2", "b2", "sheet2", ZonedDateTime.now().plusSeconds(10))
      val program = for {
        service <- ZIO.service[EmailService]
        _ <- service.create(email1)
        _ <- service.create(email2)
        emails <- service.getByCreationDateDesc()
      } yield assert(emails.head.subject)(equalTo("s2"))

      program.provideLayer(testLayer)
    }
  )
}
