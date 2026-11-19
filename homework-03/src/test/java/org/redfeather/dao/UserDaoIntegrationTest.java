package org.redfeather.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.redfeather.model.User;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class UserDaoIntegrationTest {

    @Container
    private static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    private static SessionFactory factory;

    private UserDAO userDAO;

    @BeforeAll
    static void init() {
        factory = new Configuration()
                .addAnnotatedClass(User.class)
                .setProperty(
                        "hibernate.connection.url",
                        postgres.getJdbcUrl()
                )
                .setProperty(
                        "hibernate.connection.username",
                        postgres.getUsername()
                )
                .setProperty(
                        "hibernate.connection.password",
                        postgres.getPassword()
                )
                .setProperty(
                        "hibernate.connection.driver_class",
                        "org.postgresql.Driver"
                )
                .setProperty(
                        "hibernate.hbm2ddl.auto",
                        "create-drop"
                )
                .setProperty(
                        "hibernate.show_sql",
                        "true"
                )
                .buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        userDAO = new HibernateUserDAO(factory);

        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                session.createMutationQuery("delete from User").executeUpdate();
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    @Test
    void shouldCreateUser() {
        User user = new User("Andrey", "andrey@mail.com", 40);

        User savedUser = userDAO.createUser(user);

        assertNotNull(savedUser.getId());
        User found = userDAO.getUserById(savedUser.getId());

        assertNotNull(found);
        assertEquals("Andrey", found.getName());
        assertEquals(40, found.getAge());
        assertEquals("andrey@mail.com", found.getEmail());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    void shouldFindUserByName() {
        userDAO.createUser(new User("Andrey", "first@mail.com", 40));
        userDAO.createUser(new User("Alex", "alex@mail.com", 30));
        userDAO.createUser(new User("Andrey", "second@mail.com", 25));

        List<User> result = userDAO.findByName("Andrey");

        assertEquals(2, result.size());
        assertEquals("Andrey", result.get(0).getName());
        assertEquals("Andrey", result.get(1).getName());
    }

    @Test
    void shouldUpdateUser() {
        User user = userDAO.createUser(new User("Andrey", "andrey@mail.com", 40));
        LocalDateTime originalCreatedAt =
                userDAO.getUserById(user.getId()).getCreatedAt();
        User newUser = new User("Alex", "alex@mail.com", 35);

        userDAO.updateUser(user.getId(), newUser);

        User updated = userDAO.getUserById(user.getId());
        assertNotNull(updated);
        assertEquals(user.getId(), updated.getId());
        assertEquals("Alex", updated.getName());
        assertEquals(35, updated.getAge());
        assertEquals("alex@mail.com", updated.getEmail());
        assertEquals(originalCreatedAt, updated.getCreatedAt());
    }

    @Test
    void shouldReturnNullWhenUserToUpdateDoesNotExist() {
        User newUser = new User("Andrey", "andrey@mail.com", 40);

        User result = userDAO.updateUser(999L, newUser);

        assertNull(result);
    }

    @Test
    void shouldDeleteUser() {
        User user = userDAO.createUser(new User("Andrey", "andrey@mail.com", 40));

        boolean result = userDAO.deleteUser(user.getId());

        assertTrue(result);
        assertNull(userDAO.getUserById(user.getId()));
    }

    @Test
    void shouldReturnFalseWhenUserToDeleteDoesNotExist() {
        boolean result = userDAO.deleteUser(999L);

        assertFalse(result);
    }

    @Test
    void shouldReturnAllUsers() {
        userDAO.createUser(new User("Andrey", "andrey@mail.com", 40));
        userDAO.createUser(new User("Alex", "alex@mail.com", 30));

        List<User> result = userDAO.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNameNotFound() {
        userDAO.createUser(new User("Andrey", "andrey@mail.com", 40));

        List<User> result = userDAO.findByName("Unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() {
        List<User> result = userDAO.getAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindUserByAge() {
        userDAO.createUser(new User("Andrey", "andrey@mail.com", 40));
        userDAO.createUser(new User("Alex", "alex@mail.com", 30));
        userDAO.createUser(new User("Bob", "bob@mail.com", 40));

        List<User> findUsers = userDAO.findByAge(40);

        assertNotNull(findUsers);
        assertEquals(2, findUsers.size());
        assertEquals(40, findUsers.get(0).getAge());
        assertEquals(40, findUsers.get(1).getAge());
    }

    @Test
    void shouldReturnEmptyListWhenAgeNotFound() {
        userDAO.createUser(new User("Andrey", "andrey@mail.com", 40));

        List<User> findUsers = userDAO.findByAge(999);

        assertTrue(findUsers.isEmpty());
    }

    @Test
    void shouldFindUserByEmail() {

        User user = userDAO.createUser(new User("Alex", "alex@mail.com", 30));

        List<User> findUser = userDAO.findByEmail("alex@mail.com");

        assertNotNull(findUser);
        assertEquals(1, findUser.size());
        assertEquals("alex@mail.com", findUser.getFirst().getEmail());
        assertEquals(user.getId(), findUser.getFirst().getId());
    }

    @Test
    void shouldReturnEmptyListWhenEmailNotFound() {
        userDAO.createUser(new User("Alex", "alex@mail.com", 30));

        List<User> findUser = userDAO.findByEmail("unknown@mail.com");

        assertTrue(findUser.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        userDAO.createUser(new User("Alex", "alex@mail.com", 30));

        boolean result = userDAO.existsByEmail("alex@mail.com");

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenEmailNotExist() {
        boolean result = userDAO.existsByEmail("unknown@mail.com");

        assertFalse(result);
    }

    @Test
    void shouldIgnoreCurrentUserWhenCheckingEmail() {
        User user = userDAO.createUser(new User("Alex", "alex@mail.com", 30));

        boolean result = userDAO.existsByEmailAndIdNot(user.getEmail(), user.getId());

        assertFalse(result);
    }

    @Test
    void shouldDetectEmailBelongingToAnotherUser() {
        User first = userDAO.createUser(new User("Alex", "alex@mail.com", 30));
        User second = userDAO.createUser(new User("Bob", "bob@mail.com", 30));

        boolean result = userDAO.existsByEmailAndIdNot(second.getEmail(), first.getId());

        assertTrue(result);
    }

    @Test
    void shouldRejectDuplicateEmailBelongingAnotherUser() {
        String email = "alex@mail.com";
        userDAO.createUser(new User("Alex", email, 30));

        User userDuplicate = new User("Bob", email, 30);
        assertThrows(
                ConstraintViolationException.class,
                () -> userDAO.createUser(userDuplicate)
        );

        assertEquals(1, userDAO.getAllUsers().size());
    }

    @Test
    void shouldFindByCreatedAtBetween() {
        User user = userDAO.createUser(new User("Alex", "alex@mail.com", 30));

        LocalDateTime from = user.getCreatedAt().toLocalDate().atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        List<User> result = userDAO.findByCreatedAtBetween(from, to);

        assertEquals(1, result.size());
        assertEquals(user.getId(), result.getFirst().getId());
    }

    @Test
    void whenFindByOutsideDiapasonShouldReturnEmptyList() {
        User user = userDAO.createUser(new User("Alex", "alex@mail.com", 30));

        LocalDateTime from = user.getCreatedAt().toLocalDate().atStartOfDay().plusDays(1);
        LocalDateTime to = from.plusDays(1);

        List<User> result = userDAO.findByCreatedAtBetween(from, to);

        assertTrue(result.isEmpty());
    }

    @AfterAll
    static void tearDown() {
        if(factory!=null) {
            factory.close();
        }
    }

}
