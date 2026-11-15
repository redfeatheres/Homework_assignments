import org.redfether.dao.HibernateUserDAO;
import org.redfether.dao.UserDAO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redfether.service.UserService;
import org.redfether.ui.ConsoleMenu;
import org.redfether.util.HibernateUtil;

import java.util.Scanner;

public class Main {
    private static final Logger log =
            LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Application starting");

        try (SessionFactory factory = HibernateUtil.getSessionFactory();
             Scanner scanner = new Scanner(System.in))
        {
            UserDAO userDAO = new HibernateUserDAO(factory);
            UserService userService = new UserService(userDAO);
            new ConsoleMenu(userService, scanner).run();
        } catch (ExceptionInInitializerError | RuntimeException e) {
            log.error("Application failed", e);
            System.out.println(
                    "Application failed. See logs/user-service.log for details."
            );
        } finally {
            log.info("Application stopped");
        }
    }
}
