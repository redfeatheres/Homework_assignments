package org.redfether.validation;

import org.redfether.model.User;

import java.util.regex.Pattern;

public class UserValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private UserValidator() {}

    public static void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }
        validateName(user.getName());
        validateEmail(user.getEmail());
        validateAge(user.getAge());
    }

    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID must be a positive number."
            );
        }
    }

    public static void validateName(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name cannot be null or empty"
            );
        }
        if (name.length() > 256) {
            throw new IllegalArgumentException(
                    "Name must not exceed 256 characters."
            );
        }
    }

    public static void validateAge(Integer age) {
        if(age == null || age < 0) {
            throw new IllegalArgumentException(
                    "Age must be a non-negative integer."
            );
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be null or empty"
            );
        }
        if (email.length() > 256) {
            throw new IllegalArgumentException(
                    "Email must not exceed 256 characters."
            );
        }
        if(!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Email address is invalid"
            );
        }
    }
}
