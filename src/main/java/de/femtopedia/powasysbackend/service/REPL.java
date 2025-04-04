package de.femtopedia.powasysbackend.service;

import de.femtopedia.powasysbackend.Main;
import de.femtopedia.powasysbackend.api.CachedStorage;
import de.femtopedia.powasysbackend.api.DataEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;

/**
 * Utility class for managing the user interaction.
 */
public record REPL(BufferedReader reader, CachedStorage storage) {

    /**
     * Initializes and starts the REPL Shell and waits for user input to
     * process.
     *
     * @throws IOException If some input/output error occurs.
     */
    public void startREPL() throws IOException {
        boolean quit = false;

        while (!quit) {
            String input = reader.readLine();

            if (input == null) {
                break;
            }

            if (!input.isBlank()) {
                quit = execute(input.toLowerCase(Locale.ROOT));
            }
        }
    }

    /**
     * Parses the given tokens and executes their corresponding command and
     * returns whether the REPL should exit.
     *
     * @param input The command.
     * @return {@code true} iff the program should shut down, {@code false}
     * otherwise.
     */
    private boolean execute(String input) {
        switch (input) {
            case "a":
            case "add":
                addDebugData();
                break;
            case "c":
            case "clear":
                clearQueue();
                break;
            case "p":
            case "print":
                printQueue();
                break;
            case "h":
            case "help":
                printHelp();
                break;
            case "q":
            case "quit":
                return true;
            default:
                System.err.println(ERR_CMD_NOT_EXISTS);
                break;
        }
        return false;
    }

    /**
     * Stores debug data in the database.
     */
    public void addDebugData() {
        if (!Main.IS_DEV_ENV) {
            System.err.println(ERR_ONLY_DEV_CMD);
            return;
        }

        try {
            storage.store(new DataEntry(3, "", "", 4, 45.3, 54.2, 43, 54.6, 89.5, 10, 36));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the current MySQL queue.
     */
    public void clearQueue() {
        storage.clearQueue();
    }

    /**
     * Prints the current MySQL queue.
     */
    public void printQueue() {
        System.out.println("[");
        storage.getQueue().forEach(d -> System.out.println("  " + d));
        System.out.println("]");
    }

    /**
     * Prints a helping text to show the correct usage of this program.
     */
    public void printHelp() {
        // Format for OS-independent line breaks.
        System.out.printf(INFO_HELP_TEXT);
    }

    private static final String ERR_CMD_NOT_EXISTS =
            "Command doesn't exist! For more info, type HELP.";

    private static final String ERR_ONLY_DEV_CMD = "This command is only available in a Dev Environment!";

    private static final String INFO_HELP_TEXT = """
            HELP      Prints this text.
            CLEAR     Clears the current queue.
            PRINT     Prints the current queue.
            QUIT      Quits this software.
            
            Summary:
            This program provides useful commands for debugging
            the PowaSys Backend software.
            """;

}
