import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.example.back.service.*
import com.example.back.repositories.*
import io.getquill.jdbczio.Quill

object UserServiceSpec extends ZIOSpecDefault {

  val mockRepoLayer = ZLayer.succeed(new MockUserRepository)

  val dummyQuillLayer: ZLayer[Any, Nothing, Quill.Postgres[io.getquill.SnakeCase]] =
    ZLayer.succeed(null.asInstanceOf[Quill.Postgres[io.getquill.SnakeCase]])

  val testLayer = mockRepoLayer ++ dummyQuillLayer >>> UserServiceLive.layer

  def spec = suite("UserService")(
    test("register and find user") {
      val program = for {
        service <- ZIO.service[UserService]
        user <- service.register("John", "john@example.com", "pass123", None)
        found <- service.findByEmail("john@example.com")
      } yield assert(found.map(_.email))(isSome(equalTo("john@example.com")))

      program.provideLayer(testLayer)
    }
  )
}
