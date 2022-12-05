import dto.User;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.assertj.core.api.*;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.*;

//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private UserService userService;

    @BeforeAll
    /*static*/ void init(){
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        MatcherAssert.assertThat(users, empty());
        assertTrue(users.isEmpty(), () -> "Userlist should be empty");
    }

    @Test
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
    void loginSuccesIfUserPresent() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

        assertThat(maybeUser).isPresent();
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        assertTrue(maybeUser.isPresent());without AssertJ
//        maybeUser.ifPresent(user -> assertEquals(IVAN, user));without AssertJ
    }

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

    @Test
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(IVAN);
        var maybeUser = userService.login(IVAN.getUsername(), "dummy");

        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void loginFailIfUserDoesNotExist() {
        userService.add(IVAN);
        var maybeUser = userService.login("dummy", IVAN.getPassword());

        assertTrue(maybeUser.isEmpty());
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    /*static*/ void closeConnectionPool() {
        System.out.println("After all: " + this);
    }

}
