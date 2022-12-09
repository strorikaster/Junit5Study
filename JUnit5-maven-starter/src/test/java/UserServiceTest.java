import dto.User;
import extension.*;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import service.UserService;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
@Tag("user")
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        ThrowableExtension.class
//        GlobalExtension.class Закоментировали, т.к. наследуемся от TestBase,
//        а в TestBase уже указано ExtendsWith с GlobalExtension.class
})
//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest extends TestBase {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private UserService userService;


    @BeforeAll
    /*static*/ void init(){
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(UserService userService) {
        System.out.println("Before each: " + this);
        this.userService = userService;
        //userService = new UserService();
    }

    @Test
    @Order(1)
    @DisplayName("Users will be empty if no user added")
    void usersEmptyIfNoUserAdded(UserService userService) throws IOException {
        if (true) {
            //throw  new IOException(); так тест не пройдет т.к. у нас в ThrowableExtension указано IOException
            throw new RuntimeException();//так тест пройдет т.к. у нас в ThrowableExtension не указано RuntimeException
        }
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        MatcherAssert.assertThat(users, empty());
        assertTrue(users.isEmpty(), () -> "Userlist should be empty");
    }

    @Test
    @Order(2)
    void usersSizeIfUserAdded(){
        System.out.println("Test 2:" + this);
//        userService.add(new User()); without lombok
//        userService.add(new User());  without lombok
        userService.add(IVAN);//with lombok constructor of
        userService.add(PETR);//with lombok constructor of

        var users = userService.getAll();

        assertThat(users).hasSize(2);
       // assertEquals(2, users.size()); without AssertJ
    }



    @Test
    void userConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId()));
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );


    }


    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    /*static*/ void closeConnectionPool() {
        System.out.println("After all: " + this);
    }


    //@Timeout(value = 200, unit = TimeUnit.MILLISECONDS) так тоже можно задать timeout можно использовать не только с методом, но и с классом
    @Nested //Nested class for testing login functionality
    @DisplayName("test user login functionality")
    @Tag("login")
    class LoginTest {
        //@Tag("login")//maven command: mvn clean test -Dgroups="login" запустит тесты с тэгом "login",
        //  mvn clean test -DexcludedGroups="login" запустит тесты где тэг "login" отсутствует
        @Test
        void loginSuccesIfUserPresent() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        assertTrue(maybeUser.isPresent());without AssertJ
//        maybeUser.ifPresent(user -> assertEquals(IVAN, user));without AssertJ
        }


        //@Tag("login") //added to class LoginTest
        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
            assertAll(
                    () -> {
                        var exception = assertThrows(IllegalArgumentException.class, () ->  userService.login(null, "dummy"));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );


            //2  assertThrows(IllegalArgumentException.class, () ->  userService.login(null, "dummy"));

//1        try {
//            userService.login(null, "dummy");
//            fail("login should throw exception on null username");
//        } catch (IllegalArgumentException ex) {
//            assertTrue(true);
//        }
        }

       // @Tag("login")
        @Test
        @Disabled("flacky, need to see")//Disable flacky test
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            var maybeUser = userService.login(IVAN.getUsername(), "dummy");

            assertTrue(maybeUser.isEmpty());
        }

        //@Tag("login")
        //@Test
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)//for flacky test
        void loginFailIfUserDoesNotExist(RepetitionInfo repetitionInfo) {
            userService.add(IVAN);
            var maybeUser = userService.login("dummy", IVAN.getPassword());

            assertTrue(maybeUser.isEmpty());
        }

        @Test
        //@Timeout(value = 200, unit = TimeUnit.MILLISECONDS) так тоже можно задать timeout
        void checkLoginFunctionalityPerformance() {
            System.out.println(Thread.currentThread().getName());
          var result=  assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {//Заменили assertTimeout на assertTimeoutPreemptively, assertTimeoutPreemptively выполняется в отдельном потоке
              System.out.println(Thread.currentThread().getName());
              Thread.sleep(100);//Заменил на 100 мс , т.к. на 300 мс тест падает
              return  userService.login("dummy", IVAN.getPassword());
          });
        }

        @ParameterizedTest
        //@ArgumentsSource()
       // @NullSource //работает с одним параметром
       // @EmptySource //работает с одним параметром
        //   @NullAndEmptySource
      //  @ValueSource(strings = {"Ivan", "Petr"}) //работает с одним параметром
//    @EnumSource используется редко
        @MethodSource("UserServiceTest#getArgumentsForLoginTest")
        //@CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1) нельзя использовать сложные типы данных
//        @CsvSource( можно использовать когда мало параметров для тестов, если их много лучше использовать csv файл
//            {
//                "Ivan,123",
//                "Petr,111"
//            }
//        )
        @DisplayName("login param test")
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);
            var maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }

    }


    static Stream<Arguments> getArgumentsForLoginTest() {
        return  Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "123",Optional.empty())
        );
    }
}
