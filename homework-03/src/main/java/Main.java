import org.redfeather.dao.HibernateUserDAO;
import org.redfeather.dao.UserDAO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redfeather.service.UserService;
import org.redfeather.ui.ConsoleMenu;
import org.redfeather.util.HibernateUtil;

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
