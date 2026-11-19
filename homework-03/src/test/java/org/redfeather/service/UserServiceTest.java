package org.redfeather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redfeather.dao.UserDAO;
import org.redfeather.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDAO);
    }

    @Test
    public void shouldCreateUserWhenDataIsValid() {
        User user = new User("Andrey", "andrey@example.com", 30);
        when(userDAO.existsByEmail(user.getEmail())).thenReturn(false);
        when(userDAO.createUser(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        verify(userDAO).existsByEmail(user.getEmail());
        verify(userDAO).createUser(user);
    }

    @Test
    public void shouldRejectCreationWhenEmailAlreadyExists() {
        User user = new User("Andrey", "andrey@example.com", 30);
        when(userDAO.existsByEmail(user.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userDAO, never()).createUser(any(User.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    public void shouldRejectCreationWhenNameIsBlankOrNull(String name) {
        User user = new User(name, "andrey@example.com", 30);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Name cannot be null or empty", exception.getMessage());
        verifyNoInteractions(userDAO);
    }

    @Test
    public void shouldNotCallDaoWhenNameExceed256Characters() {
        String name = "A".repeat(265);
        User user = new User(name, "andrey@example.com", 30);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Name must not exceed 256 characters.", exception.getMessage());
        verifyNoInteractions(userDAO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    public void shouldRejectCreationWhenEmailIsBlankOrNull(String email) {
        User user = new User("Andrey", email, 30);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Email cannot be null or empty", exception.getMessage());
        verifyNoInteractions(userDAO);
    }

    @Test
    public void shouldCreatedUserWhenEmailEquals256Characters() {
        String email = "a".repeat(244) + "@example.com";
        User user = new User("Andrey", email, 30);
        when(userDAO.existsByEmail(user.getEmail())).thenReturn(false);
        when(userDAO.createUser(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        verify(userDAO, times(1)).existsByEmail(user.getEmail());
        verify(userDAO, times(1)).createUser(user);
    }

    @Test
    public void shouldRejectCreationWhenEmailIsTooLong() {
        String email = "a".repeat(245) + "@example.com";
        User user = new User("Andrey", email, 30);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Email must not exceed 256 characters.", exception.getMessage());
        verifyNoInteractions(userDAO);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "andreyexample.com",
            "andrey@",
            "@example.com",
            "andrey@example",
            "and rey@example.com"
    })
    void shouldRejectCreationWhenEmailFormatIsInvalid(String email) {
        User user = new User("Andrey", email, 30);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Email address is invalid", exception.getMessage());
        verifyNoInteractions(userDAO);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    void shouldRejectCreationWhenAgeIsInvalid(Integer age) {
        User user = new User("Andrey", "andrey@example.com", age);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Age must be a non-negative integer.", exception.getMessage());
        verifyNoInteractions(userDAO);
    }

    @Test
    void shouldCreateUserWhenAgeIsZero() {
        User user = new User("Andrey", "andrey@example.com", 0);
        when(userDAO.existsByEmail(user.getEmail())).thenReturn(false);
        when(userDAO.createUser(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        verify(userDAO, times(1)).existsByEmail(user.getEmail());
        verify(userDAO, times(1)).createUser(user);
    }

    @Test
    void whenFindUserByExistingIdShouldReturnUser() {
        Long id = 1L;
        User user = new User("Andrey", "andrey@example.com", 30);
        when(userDAO.getUserById(id)).thenReturn(user);

        User result = userService.getUserById(id);

        assertSame(user, result);
        verify(userDAO, times(1)).getUserById(id);
    }

    @Test
    void whenFindUserByNotExistingIdShouldReturnNull() {
        Long id = 1L;
        when(userDAO.getUserById(id)).thenReturn(null);

        User result = userService.getUserById(id);

        assertNull(result);
        verify(userDAO, times(1)).getUserById(id);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {-1L, -100L, 0})
    void whenFindByInvalidIdShouldThrowException(Long id) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(id)
        );

        assertEquals("ID must be a positive number.", exception.getMessage());
        verifyNoInteractions(userDAO);
    }

    @Test
    void whenGetAllUsersShouldReturnAllUsers() {
        User user = new User("Andrey", "andrey@example.com", 0);
        User user2 = new User("John", "john@example.com", 20);
        List<User> expected = List.of(user, user2);
        when(userDAO.getAllUsers()).thenReturn(expected);

        List<User> result = userService.getAllUsers();

        assertEquals(expected, result);
        verify(userDAO).getAllUsers();
    }

    @Test
    void whenGetAllUsersShouldReturnEmptyList() {
        when(userDAO.getAllUsers()).thenReturn(Collections.emptyList());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
        verify(userDAO).getAllUsers();
    }

    @Test
    void whenUpdateUserShouldUpdateUser() {
        Long id = 1L;
        User user = new User("Andrey", "andrey@example.com", 30);
        when(userDAO.getUserById(id)).thenReturn(user);
        when(userDAO.existsByEmailAndIdNot(user.getEmail(), id)).thenReturn(false);
        when(userDAO.updateUser(id, user)).thenReturn(user);

        User result = userService.updateUser(id, user);

        assertSame(user, result);
        verify(userDAO).getUserById(id);
        verify(userDAO).updateUser(id, user);
    }

    @Test
    void whenUpdateUserByNotExistingIdShouldReturnNull() {
        Long id = 1L;
        User user = new User("Andrey", "andrey@example.com", 30);
        when(userDAO.getUserById(id)).thenReturn(null);

        User result = userService.updateUser(id, user);

        assertNull(result);
        verify(userDAO).getUserById(id);
        verifyNoMoreInteractions(userDAO);
    }

    @Test
    void whenUpdateUserWithAlreadyExistingEmailShouldThrowException() {
        Long id = 1L;
        User user = new User("Andrey", "andrey@example.com", 30);
        when(userDAO.getUserById(id)).thenReturn(user);
        when(userDAO.existsByEmailAndIdNot(user.getEmail(), id))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(id, user)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userDAO, never()).updateUser(anyLong(), any(User.class));
    }

    @Test
    void whenDeleteUserShouldDeleteUser() {
        Long id = 1L;
        when(userDAO.deleteUser(id)).thenReturn(true);

        boolean result = userService.deleteUser(id);

        assertTrue(result);
        verify(userDAO).deleteUser(id);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {-1L, -100L, 0})
    void whenDeleteUserWithNotExistingIdShouldThrowException(Long id) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(id)
        );

        assertEquals("ID must be a positive number.", exception.getMessage());
        verifyNoMoreInteractions(userDAO);
    }

    @Test
    void whenDeleteUserWithNotExistingIdShouldReturnFalse() {
        Long id = 1L;
        when(userDAO.deleteUser(id)).thenReturn(false);

        boolean result = userService.deleteUser(id);

        assertFalse(result);
        verify(userDAO).deleteUser(id);
        verifyNoMoreInteractions(userDAO);
    }

    @Test
    void whenFindByNameShouldReturnUser() {
        String name = "Andrey";
        User user = new User(name, "andrey@example.com", 30);
        User user2 = new User(name, "andrey2@example.com", 30);
        List<User> expected = List.of(user, user2);
        when(userDAO.findByName(name)).thenReturn(expected);

        List<User> result = userService.findByName(name);

        assertEquals(expected, result);
        verify(userDAO).findByName(name);
    }

    @Test
    void whenFindByNotExistingNameShouldReturnEmptyList() {
        when(userDAO.findByName("test")).thenReturn(Collections.emptyList());

        List<User> result = userService.findByName("test");

        assertEquals(Collections.emptyList(), result);
        verify(userDAO).findByName("test");
    }

    @Test
    void whenFindByEmailShouldReturnUser() {
        String email = "andrey@example.com";
        User user = new User("Andrey", email, 30);
        List<User> expected = List.of(user);
        when(userDAO.findByEmail(email)).thenReturn(expected);

        List<User> result = userService.findByEmail(email);

        assertEquals(expected, result);
        verify(userDAO).findByEmail(email);
    }

    @Test
    void whenFindByNotExistingEmailShouldReturnEmptyList() {
        String email = "missing@example.com";
        when(userDAO.findByEmail(email)).thenReturn(Collections.emptyList());

        List<User> result = userService.findByEmail(email);

        assertTrue(result.isEmpty());
        verify(userDAO).findByEmail(email);
    }

    @Test
    void whenFindByAgeShouldReturnUser() {
        int age = 30;
        User user = new User("Andrey", "andrey@example.com", 30);
        User user2 = new User("Andrey2", "andrey2@example.com", 30);
        List<User> expected = List.of(user, user2);
        when(userDAO.findByAge(age)).thenReturn(expected);

        List<User> result = userService.findByAge(age);

        assertEquals(expected, result);
        verify(userDAO).findByAge(age);
    }

    @Test
    void whenFindByNotExistingAgeShouldReturnEmptyList() {
        when(userDAO.findByAge(30)).thenReturn(Collections.emptyList());

        List<User> result = userService.findByAge(30);

        assertEquals(Collections.emptyList(), result);
        verify(userDAO).findByAge(30);
    }

    @Test
    void whenFindByDateShouldReturnUser() {
        LocalDate date = LocalDate.of(2020, 1, 1);
        LocalDateTime from = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 1, 2, 0, 0);
        User user = new User("Andrey", "andrey@example.com", 30);
        User user2 = new User("Andrey2", "andrey2@example.com", 30);
        List<User> expected = List.of(user, user2);
        when(userDAO.findByCreatedAtBetween(from, to)).thenReturn(expected);

        List<User> result = userService.findByCreatedDate(date);

        assertEquals(expected, result);
        verify(userDAO).findByCreatedAtBetween(from, to);
    }

    @Test
    void whenFindByNullDateShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findByCreatedDate(null)
        );

        assertEquals("Date must not be null.", exception.getMessage());
        verifyNoMoreInteractions(userDAO);
    }
}
