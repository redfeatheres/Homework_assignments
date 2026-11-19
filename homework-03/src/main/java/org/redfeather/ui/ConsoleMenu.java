package org.redfeather.ui;

import jakarta.persistence.PersistenceException;
import org.redfeather.model.User;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redfeather.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleMenu {
    private static final Logger log =
            LoggerFactory.getLogger(ConsoleMenu.class);
    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final UserService userService;
    private final Scanner scanner;

    public ConsoleMenu(UserService userService, Scanner scanner) {
        this.userService = userService;
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            printMenu();
            if (!scanner.hasNextLine()) {
                return;
            }
            String choice = scanner.nextLine().strip();
            try {
                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> findUser();
                    case "3" -> showAllUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "0" -> {
                        System.out.println("Exit");
                        return;
                    }
                    default -> {
                        System.out.println("Unknown choice");
                    }
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-MM-dd, for example 2026-11-15.");
            } catch (NumberFormatException e) {
                System.out.println("Enter an integer");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (PersistenceException e) {
                handleDatabaseError(e);
            } catch (NoSuchElementException e) {
                System.out.println("Input ended. Exit.");
                return;
            }
        }
    }

    private void createUser() {
        String name = readName();
        String email = readEmail();
        int age = readAge();
        User user = new User(name, email, age);
        User savedUser = userService.createUser(user);
        System.out.println(
                "User has been created. ID: " + savedUser.getId()
        );
    }

    private void findUser() {
        while (true) {
            printFindMenu();
            if (!scanner.hasNextLine()) {
                return;
            }
            String choice = scanner.nextLine().strip();
            try {
                switch (choice) {
                    case "1" -> {
                        User user = userService.getUserById(readId());
                        if(user == null) {
                            System.out.println("User not found");
                        } else {
                            printUsers(List.of(user));
                        }
                    }
                    case "2" -> printUsers(
                            userService.findByName(readName())
                    );
                    case "3" -> printUsers(
                            userService.findByAge(readAge())
                    );
                    case "4" -> printUsers(
                            userService.findByCreatedDate(readDate())
                    );
                    case "5" -> printUsers(
                            userService.findByEmail(readEmail())
                    );
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Unknown choice.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-MM-dd, for example 2026-11-15.");
            } catch (NumberFormatException e) {
                System.out.println("Enter an integer");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (PersistenceException e) {
                handleDatabaseError(e);
            }
        }
    }

    private void deleteUser() {
        Long id = readId();

        String result = userService.deleteUser(id)
                ? "User ID: " + id + " has been deleted."
                : "User not found.";

        System.out.println(result);
    }

    private void updateUser() {
        Long id = readId();
        String name = readName();
        String email = readEmail();
        int age = readAge();
        User user = new User(name, email, age);
        User updateUser = userService.updateUser(id, user);
        String result = updateUser == null
                ? "User not found."
                : "User has been updated. ID: " + updateUser.getId();
        System.out.println(result);
    }

    private void showAllUsers() {
        printUsers(userService.getAllUsers());
    }

    private Long readId() {
        System.out.print("Enter ID: ");
        return Long.parseLong(scanner.nextLine().strip());
    }

    private String readName() {
        System.out.println("Enter user name: ");
        return scanner.nextLine().strip();
    }

    private String readEmail() {
        System.out.println("Enter user email: ");
        return scanner.nextLine().strip();
    }

    private int readAge() {
        System.out.print("Enter user age: ");
        return Integer.parseInt(scanner.nextLine().strip());
    }

    private LocalDate readDate() {
        System.out.print("Enter creation date (yyyy-MM-dd): ");
        return LocalDate.parse(scanner.nextLine().strip());
    }

    private void printUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("Users not found.");
            return;
        }

        String separator = "-".repeat(93);

        System.out.println(separator);

        System.out.printf(
                "%-6s | %-20s | %-30s | %-5s | %-16s%n",
                "ID", "NAME", "EMAIL", "AGE", "CREATED"
        );

        System.out.println(separator);

        for (User user : users) {
            printUserRow(user);
        }

        System.out.println(separator);
        System.out.println("Total: " + users.size());
    }

    private void printUserRow(User user) {
        String createdAt = user.getCreatedAt() == null
                ? "-"
                : user.getCreatedAt().format(DATE_FORMAT);

        System.out.printf(
                "%-6d | %-20.20s | %-30.30s | %-5d | %-16s%n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                createdAt
        );
    }

    private void printMenu() {
        System.out.println("""
                ----------------
                SELECT AN ACTION:
                ----------------
                  1. CREATE user
                  2. FIND users
                  3. SHOW all users
                  4. UPDATE user
                  5. DELETE user
                  0. Exit
                ----------------
                """);
        System.out.print("YOUR CHOICE: ");
    }

    private void printFindMenu() {
        System.out.println("""
                --------------
                FIND USERS BY:
                --------------
                  1. ID
                  2. Name
                  3. Age
                  4. Creation date
                  5. Email
                  0. Back
                --------------
                """);
        System.out.print("YOUR CHOICE: ");
    }

    private void handleDatabaseError(PersistenceException e) {
        log.error("Database operation failed", e);
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                if ("users_email_unique".equals(
                        violation.getConstraintName())) {
                    System.out.println("Email already exists.");
                } else {
                    System.out.println(
                            "Cannot save changes: a database constraint was violated."
                    );
                }
                return;
            }
            cause = cause.getCause();
        }
        System.out.println(
                "Database operation failed. Check that PostgreSQL is running."
        );
    }
}
