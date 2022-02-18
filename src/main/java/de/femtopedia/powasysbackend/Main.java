package de.femtopedia.powasysbackend;

import de.femtopedia.powasysbackend.api.SerialPort;
import de.femtopedia.powasysbackend.api.Storage;
import de.femtopedia.powasysbackend.config.Config;
import de.femtopedia.powasysbackend.rest.RestAPI;
import de.femtopedia.powasysbackend.serial.SerialReader;
import de.femtopedia.powasysbackend.sql.MySQLStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public final class Main {

    private static Config config;

    private static Storage storage;

    private static RestAPI restAPI;

    private static SerialReader serialReader;

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
        try {
            config = Config.readOrInit(Path.of("config.json"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (config == null) {
            System.err.println("There was a problem with your configuration file.");
            return false;
        }

        try {
            storage = new MySQLStorage(config.getMySQL());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        restAPI = new RestAPI(storage).start(config.getRestApiPort());

        serialReader = new SerialReader(storage);

        for (SerialPort serialPort : config.getSerialPorts()) {
            try {
                serialReader.startListening(serialPort);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private static void loop() {
        while (true) {
            try {
                if (System.in.read() < 0) break;
            } catch (IOException ignored) {
            }
        }
    }

    private static void shutdown() {
        if (serialReader != null) {
            try {
                serialReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (restAPI != null) {
            try {
                restAPI.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (storage != null) {
            try {
                storage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Main() {
        throw new UnsupportedOperationException();
    }

}
