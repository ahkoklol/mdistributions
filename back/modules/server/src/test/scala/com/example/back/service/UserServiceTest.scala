import zio.*
import zio.test.*
import zio.test.Assertion.*
import java.time.ZonedDateTime
import com.example.back.*
import com.example.back.repositories.*
import com.example.back.service.*

class MockUserRepository extends UserRepository {
  private var store = Map.empty[Long, UserEntity]
  private var counter = 0L

  override def create(newUser: NewUserEntity): Task[UserEntity] = ZIO.succeed {
    if (store.values.exists(_.email == newUser.email)) throw new java.sql.SQLException("duplicate", "23505")
    counter += 1
    val user = UserEntity(
      id = counter,
      name = newUser.name,
      email = newUser.email,
      hashedPassword = newUser.hashedPassword,
      googleSheetsLink = newUser.googleSheetsLink,
      creationDate = newUser.creationDate
    )
    store += (counter -> user)
    user
  }

  override def findByEmail(email: String): Task[Option[UserEntity]] =
    ZIO.succeed(store.values.find(_.email == email))

  override def getById(id: Long): Task[Option[UserEntity]] =
    ZIO.succeed(store.get(id))

  override def update(id: Long, op: UserEntity => UserEntity): Task[UserEntity] =
    store.get(id) match
      case Some(user) =>
        val updated = op(user)
        store += (id -> updated)
        ZIO.succeed(updated)
      case None => ZIO.fail(UserNotFoundException(id.toString))

  override def delete(id: Long): Task[UserEntity] =
    store.get(id) match
      case Some(user) =>
        store -= id
        ZIO.succeed(user)
      case None => ZIO.fail(UserNotFoundException(id.toString))
}

object UserServiceSpec extends ZIOSpecDefault {

  val mockRepo = new MockUserRepository
  val userServiceLayer = ZLayer.succeed(mockRepo) >>> UserServiceLive.layer

  def spec = suite("UserServiceLive")(
    test("register user successfully") {
      val program = ZIO.serviceWithZIO[UserService](_.register("Alice", "alice@test.com", "pass123", None))
      assertZIO(program.provideLayer(userServiceLayer)) { user =>
        assert(user.id)(isGreaterThan(0L)) &&
        assert(user.email)(equalTo("alice@test.com"))
      }
    },

    test("register duplicate user fails") {
      val program = for {
        _ <- ZIO.serviceWithZIO[UserService](_.register("Bob", "bob@test.com", "pass123", None))
        second <- ZIO.serviceWithZIO[UserService](_.register("Bob2", "bob@test.com", "pass456", None)).either
      } yield second

      assertZIO(program.provideLayer(userServiceLayer))(isLeft(isSubtype[UserAlreadyExistsException](anything)))
    },

    test("login with correct credentials succeeds") {
      val program = for {
        service <- ZIO.service[UserService]
        _ <- service.register("Charlie", "charlie@test.com", "secret", None)
        user <- service.login("charlie@test.com", "secret")
      } yield user

      assertZIO(program.provideLayer(userServiceLayer))(user => assert(user.email)(equalTo("charlie@test.com")))
    },

    test("login with wrong password fails") {
      val program = for {
        service <- ZIO.service[UserService]
        _ <- service.register("Dan", "dan@test.com", "mypassword", None)
        attempt <- service.login("dan@test.com", "wrongpass").either
      } yield attempt

      assertZIO(program.provideLayer(userServiceLayer))(isLeft(isSubtype[InvalidCredentialsException](anything)))
    },

    test("getUserById returns the correct user") {
      val program = for {
        service <- ZIO.service[UserService]
        user <- service.register("Eve", "eve@test.com", "123", None)
        fetched <- service.getUserById(user.id)
      } yield fetched

      assertZIO(program.provideLayer(userServiceLayer))(user => assert(user.email)(equalTo("eve@test.com")))
    },

    test("updateUser modifies user") {
      val program = for {
        service <- ZIO.service[UserService]
        user <- service.register("Frank", "frank@test.com", "123", None)
        updated <- service.updateUser(user.id, u => u.copy(name = "FrankUpdated"))
      } yield updated

      assertZIO(program.provideLayer(userServiceLayer))(user => assert(user.name)(equalTo("FrankUpdated")))
    },

    test("deleteUser removes user") {
      val program = for {
        service <- ZIO.service[UserService]
        user <- service.register("Grace", "grace@test.com", "123", None)
        deleted <- service.deleteUser(user.id)
        remaining <- service.findByEmail("grace@test.com")
      } yield remaining

      assertZIO(program.provideLayer(userServiceLayer))(isNone)
    }
  )
}
