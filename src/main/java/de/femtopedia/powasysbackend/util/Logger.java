package de.femtopedia.powasysbackend.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Facade for a logging API. Currently: {@link java.util.logging}.
 */
public final class Logger {

    /**
     * The depth of method calls starting from the client until reaching {@link #log(Level, String, Throwable)}.
     */
    private static final int CALL_DEPTH = 3;

    /**
     * Cache of loggers for their associated classes for performance.
     */
    private static final Map<String, Logger> logMap = new HashMap<>();

    /**
     * {@link java.util.logging.Logger} proxied by this instance.
     */
    private final java.util.logging.Logger logger;

    /**
     * Constructs a new Log proxying a logger for the given class.
     *
     * @param name Fully-qualified name of the class the new Log is associated with.
     */
    private Logger(String name) {
        logger = java.util.logging.Logger.getLogger(name);
    }

    /**
     * Gets the logger instance for the class {@code clazz}.
     *
     * @param clazz The class to obtain a logger for.
     * @param <T>   The type of the class modeled by the {@link Class} object.
     * @return The logger instance for {@code clazz}.
     */
    public static <T> Logger forClass(Class<T> clazz) {
        String name = clazz.getName();
        Logger log = logMap.get(name);
        if (log == null) {
            log = new Logger(name);
            logMap.put(name, log);
        }
        return log;
    }

    /**
     * Logs an error with a given message.
     *
     * @param msg The message to log.
     */
    public void error(String msg) {
        log(Level.SEVERE, msg, null);
    }

    /**
     * Logs an error with a given message and cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the error.
     */
    public void error(String msg, Throwable cause) {
        log(Level.SEVERE, msg, cause);
    }

    /**
     * Logs a warning with a given message.
     *
     * @param msg The message to log.
     */
    public void warning(String msg) {
        log(Level.WARNING, msg, null);
    }

    /**
     * Logs a warning with a given message and cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the warning.
     */
    public void warning(String msg, Throwable cause) {
        log(Level.WARNING, msg, cause);
    }

    /**
     * Logs an information message.
     *
     * @param msg The message to log.
     */
    public void info(String msg) {
        log(Level.INFO, msg, null);
    }

    /**
     * Logs an information message with a given cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    public void info(String msg, Throwable cause) {
        log(Level.INFO, msg, cause);
    }

    /**
     * Logs a debug message.
     *
     * @param msg The message to log.
     */
    public void debug(String msg) {
        log(Level.FINEST, msg, null);
    }

    /**
     * Logs a debug message with a given cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    public void debug(String msg, Throwable cause) {
        log(Level.FINEST, msg, cause);
    }

    /**
     * Logs a debug message with given level and cause.
     *
     * @param level The log level for the message.
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    private void log(Level level, String msg, Throwable cause) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > CALL_DEPTH) {
            StackTraceElement e = stackTrace[CALL_DEPTH]; // caller
            logger.logp(level, e.getClassName(), e.getMethodName(), msg, cause);
        } else { // should never happen
            logger.log(level, msg, cause);
        }
    }

}
