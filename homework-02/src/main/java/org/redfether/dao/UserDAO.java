package org.redfether.dao;

import org.redfether.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserDAO {
    User createUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateUser(Long id, User user);

    boolean deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<User> findByName(String name);

    List<User> findByAge(Integer age);

    List<User> findByEmail(String email);

    List<User> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
