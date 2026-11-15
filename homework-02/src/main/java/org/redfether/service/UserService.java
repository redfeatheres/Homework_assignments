package org.redfether.service;

import org.redfether.dao.UserDAO;
import org.redfether.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redfether.validation.UserValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User createUser(User user) {
        UserValidator.validateUser(user);
        if(userDAO.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }
        User createdUser = userDAO.createUser(user);
        log.info("User created. ID: {}", createdUser.getId());
        return createdUser;
    }

    public User getUserById(Long id) {
        UserValidator.validateId(id);
        return userDAO.getUserById(id);
    }


    public List<User> findByName(String name) {
        UserValidator.validateName(name);
        return userDAO.findByName(name);
    }

    public List<User> findByAge(Integer age) {
        UserValidator.validateAge(age);
        return userDAO.findByAge(age);
    }

    public List<User> findByEmail(String email) {
        UserValidator.validateEmail(email);
        return userDAO.findByEmail(email);
    }

    public List<User> findByCreatedDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null.");
        }
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        return userDAO.findByCreatedAtBetween(from, to);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User updateUser(Long id, User user) {
        UserValidator.validateUser(user);
        UserValidator.validateId(id);
        if(userDAO.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new IllegalArgumentException("Email already exists");
        }
        User updatedUser = userDAO.updateUser(id, user);
        if (updatedUser == null) {
            log.info("Update skipped: user not found. ID: {}", id);
        } else {
            log.info("User updated. ID: {}", id);
        }
        return updatedUser;
    }

    public boolean deleteUser(Long id) {
        UserValidator.validateId(id);
        boolean deleted = userDAO.deleteUser(id);
        log.info("User deleted. ID: {}, deleted: {}", id, deleted);
        return deleted;
    }
}
