package org.redfether.dao;

import org.redfether.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class HibernateUserDAO implements UserDAO {

    private final SessionFactory sessionFactory;

    public HibernateUserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User createUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.persist(user);
                transaction.commit();
                return user;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public User getUserById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                User findUser = session.find(User.class, id);
                transaction.commit();
                return findUser;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<User> allUsers = session
                        .createQuery("from User", User.class)
                        .getResultList();
                transaction.commit();
                return allUsers;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public User updateUser(Long id, User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                User userForUpdate = session.find(User.class, id);
                if (userForUpdate == null) {
                    transaction.commit();
                    return null;
                }
                userForUpdate.setName(user.getName());
                userForUpdate.setEmail(user.getEmail());
                userForUpdate.setAge(user.getAge());
                transaction.commit();
                return userForUpdate;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public boolean deleteUser(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                User userForDelete = session.find(User.class, id);
                if (userForDelete == null) {
                    transaction.commit();
                    return false;
                }
                session.remove(userForDelete);
                transaction.commit();
                return true;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public List<User> findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<User> findUsers = session
                        .createQuery("""
                                        SELECT u
                                        FROM User u
                                        WHERE u.name = :name
                                        """, User.class)
                        .setParameter("name", name)
                        .getResultList();
                transaction.commit();
                return findUsers;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public List<User> findByAge(Integer age) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<User> findUsers = session
                        .createQuery("""
                                        SELECT u
                                        FROM User u
                                        WHERE u.age = :age
                                        """, User.class)
                        .setParameter("age", age)
                        .getResultList();
                transaction.commit();
                return findUsers;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public List<User> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<User> findUsers = session
                        .createQuery("""
                                        SELECT u
                                        FROM User u
                                        WHERE u.email = :email
                                        """, User.class)
                        .setParameter("email", email)
                        .getResultList();
                transaction.commit();
                return findUsers;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public List<User> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<User> findUsers = session
                        .createQuery("""
                                        SELECT u
                                        FROM User u
                                        WHERE u.createdAt >= :from
                                        AND u.createdAt < :to
                                        """, User.class)
                        .setParameter("from", from)
                        .setParameter("to", to)
                        .getResultList();
                transaction.commit();
                return findUsers;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Long count = session.createQuery(
                                """
                                SELECT COUNT(u)
                                FROM User u
                                WHERE u.email = :email
                                """,
                                Long.class
                        )
                        .setParameter("email", email)
                        .getSingleResult();
                transaction.commit();
                return count > 0;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Long count = session.createQuery(
                                """
                                SELECT COUNT(u)
                                FROM User u
                                WHERE u.email = :email AND u.id <> :id
                                """,
                                Long.class
                        )
                        .setParameter("email", email)
                        .setParameter("id", id)
                        .getSingleResult();
                transaction.commit();
                return count > 0;
            } catch (RuntimeException e) {
                rollbackSafely(transaction, e);
                throw e;
            }
        }
    }

    private void rollbackSafely(
            Transaction transaction,
            RuntimeException originalException
    ) {
        try {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        } catch (RuntimeException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
