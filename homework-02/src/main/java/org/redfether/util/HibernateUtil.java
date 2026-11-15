package org.redfether.util;

import org.redfether.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY =
            buildSessionFactory();

    public HibernateUtil() {}

    private static SessionFactory buildSessionFactory() {
        return new Configuration()
                .addAnnotatedClass(User.class)
                .buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        SESSION_FACTORY.close();
    }
}
