import zio.*
import com.example.back.*
import com.example.back.repositories.*
import com.example.back.domain.*
import java.time.ZonedDateTime

class MockEmailRepository extends EmailRepository {
  private var store = Map.empty[Long, EmailEntity]
  private var counter = 0L

  override def create(email: EmailEntity): Task[EmailEntity] = ZIO.succeed {
    counter += 1
    val newEmail = email.copy(id = counter)
    store += (counter -> newEmail)
    newEmail
  }

  override def getById(id: Long): Task[Option[EmailEntity]] =
    ZIO.succeed(store.get(id))

  override def getByCreationDateDesc(): Task[List[EmailEntity]] =
    ZIO.succeed(store.values.toList.sortBy(_.creationDate)(Ordering[ZonedDateTime].reverse))
}


object EmailServiceSpec extends ZIOSpecDefault {

  val mockRepo = new MockEmailRepository
  val emailServiceLayer = ZLayer.succeed(mockRepo) >>> EmailServiceLive.layer

  def spec = suite("EmailServiceLive")(
    test("create and retrieve email") {
      val email = EmailEntity(
        id = 0,
        userId = 42,
        subject = "Hello",
        body = "Welcome!",
        googleSheetsLink = "https://sheet.link",
        creationDate = ZonedDateTime.now()
      )

      val program = for {
        created <- ZIO.serviceWithZIO[EmailService](_.create(email))
        fetched <- ZIO.serviceWithZIO[EmailService](_.getById(created.id))
      } yield (created, fetched)

      assertZIO(program.provideLayer(emailServiceLayer)) { case (created, fetched) =>
        assert(created.id)(not(equalTo(0L))) &&
        assert(fetched.map(_.id))(isSome(equalTo(created.id)))
      }
    },

    test("get emails sorted by creation date descending") {
      val email1 = EmailEntity(0, 1, "A", "A", "link1", ZonedDateTime.now().minusDays(1))
      val email2 = EmailEntity(0, 2, "B", "B", "link2", ZonedDateTime.now())

      val program = for {
        service <- ZIO.service[EmailService]
        _ <- service.create(email1)
        _ <- service.create(email2)
        emails <- service.getByCreationDateDesc()
      } yield emails

      assertZIO(program.provideLayer(emailServiceLayer)) { emails =>
        assert(emails.map(_.subject))(equalTo(List("B", "A")))
      }
    },

    test("getById returns None for unknown ID") {
      val program = ZIO.serviceWithZIO[EmailService](_.getById(999))
      assertZIO(program.provideLayer(emailServiceLayer))(isNone)
    },

    test("create multiple emails increments IDs") {
      val email1 = EmailEntity(0, 1, "C", "C", "link3", ZonedDateTime.now())
      val email2 = EmailEntity(0, 1, "D", "D", "link4", ZonedDateTime.now())

      val program = for {
        service <- ZIO.service[EmailService]
        created1 <- service.create(email1)
        created2 <- service.create(email2)
      } yield (created1, created2)

      assertZIO(program.provideLayer(emailServiceLayer)) { case (e1, e2) =>
        assert(e1.id)(not(equalTo(e2.id)))
      }
    },

    test("getByCreationDateDesc returns empty list when no emails") {
      val emptyRepo = new MockEmailRepository // fresh repo
      val layer = ZLayer.succeed(emptyRepo) >>> EmailServiceLive.layer

      val program = ZIO.serviceWithZIO[EmailService](_.getByCreationDateDesc())
      assertZIO(program.provideLayer(layer))(isEmpty)
    }
  )
}
