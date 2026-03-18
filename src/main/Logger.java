package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        System.out.println("[" + maintenant() + "] [INFO]  " + message);
    }


    public static void warn(String message) {
        System.out.println("[" + maintenant() + "] [WARN]  " + message);
    }

    public static void error(String message, Exception e) {
        System.out.println("[" + maintenant() + "] [ERROR] " + message);
        System.out.println("  Cause : " + e.getClass().getSimpleName() + " — " + e.getMessage());

        StackTraceElement[] trace = e.getStackTrace();
        if (trace.length > 0) {
            System.out.println("  at " + trace[0]);
        }
    }

    private static String maintenant() {
        return LocalDateTime.now().format(FORMAT);
    }
}