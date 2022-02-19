package de.femtopedia.powasysbackend.service;

import de.femtopedia.powasysbackend.api.CachedStorage;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Utility class for managing the user interaction.
 */
@Data
@RequiredArgsConstructor
public class REPL {

    private final BufferedReader reader;

    private final CachedStorage storage;

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
     *         otherwise.
     */
    private boolean execute(String input) {
        switch (input) {
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

    private static final String ERR_CMD_NOT_EXISTS = "Command doesn't exist! "
            + "For more info, type HELP.";

    private static final String INFO_HELP_TEXT = ""
            + "HELP      Prints this text.%n"
            + "CLEAR     Clears the current queue.%n"
            + "PRINT     Prints the current queue.%n"
            + "QUIT      Quits this software.%n"
            + "%n"
            + "Summary:%n"
            + "This program provides useful commands for debugging%n"
            + "the PowaSys Backend software.%n";

}
