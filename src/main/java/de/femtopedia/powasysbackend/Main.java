package de.femtopedia.powasysbackend;

import de.femtopedia.powasysbackend.api.CachedStorage;
import de.femtopedia.powasysbackend.api.SerialPort;
import de.femtopedia.powasysbackend.service.DatabaseStorage;
import de.femtopedia.powasysbackend.service.REPL;
import de.femtopedia.powasysbackend.service.RestAPI;
import de.femtopedia.powasysbackend.service.SerialReader;
import de.femtopedia.powasysbackend.util.Config;
import de.femtopedia.powasysbackend.util.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.SQLException;

public final class Main {

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    public static final boolean IS_DEV_ENV = VERSION == null;

    private static final Logger LOGGER = Logger.forClass(Main.class);

    private static Config config;

    private static CachedStorage storage;

    private static RestAPI restAPI;

    private static SerialReader serialReader;

    private static REPL repl;

    public static void main(String[] args) {
        if (!init()) {
            System.err.println("Error occurred when initializing PowaSys Backend! Stopping...");
            shutdown();
            return;
        }

        loop();

        shutdown();
    }

    private static boolean init() {
        LOGGER.info("Initializing PowaSys Backend " + (IS_DEV_ENV ? "DEV" : VERSION) + "...");

        try {
            config = Config.readOrInit(Path.of("config.json"));
        } catch (IOException e) {
            LOGGER.error("Error initializing configuration", e);
            return false;
        }

        if (config == null) {
            LOGGER.error("There was a problem with your configuration file");
            return false;
        }

        try {
            storage = new DatabaseStorage(config.mySQL());
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Error initializing storage", e);
            return false;
        }

        try {
            storage.loadQueue();
        } catch (IOException e) {
            LOGGER.error("Error reading queue file", e);
        }

        int restAPIPort = config.restApiPort();
        if (restAPIPort > 0)
            restAPI = new RestAPI(storage, config.powadors()).start(restAPIPort);

        serialReader = new SerialReader(storage);

        for (SerialPort serialPort : config.serialPorts()) {
            try {
                serialReader.startListening(serialPort);
            } catch (IOException e) {
                LOGGER.error("Error initializing serial reader", e);
                return false;
            }
        }

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        repl = new REPL(stdin, storage);

        return true;
    }

    private static void loop() {
        try {
            repl.startREPL();
        } catch (IOException e) {
            LOGGER.error("Error initializing REPL", e);
        }
    }

    private static void shutdown() {
        if (serialReader != null) {
            try {
                serialReader.close();
            } catch (Exception e) {
                LOGGER.error("Error shutting down serial reader", e);
            }
        }

        if (restAPI != null) {
            try {
                restAPI.close();
            } catch (Exception e) {
                LOGGER.error("Error shutting down Rest API", e);
            }
        }

        if (storage != null) {
            try {
                storage.close();
            } catch (Exception e) {
                LOGGER.error("Error shutting down storage", e);
            }

            try {
                storage.dumpQueue();
            } catch (IOException e) {
                LOGGER.error("Error dumping queue", e);
            }
        }
    }

    private Main() {
        throw new UnsupportedOperationException();
    }

}
