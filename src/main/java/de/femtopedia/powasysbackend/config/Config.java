package de.femtopedia.powasysbackend.config;

import de.femtopedia.mysql.MySQL;
import de.femtopedia.powasysbackend.util.Util;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Config {

    private final MySQL mySQL;

    private final int restApiPort;

    private final List<String> serialPorts;

    public Config() {
        this(new MySQL("<hostname>", "<port>", "<database>", "<username>", "<password>"),
                80,
                List.of("/dev/ttyUSB0"));
    }

    public static Config readOrInit(Path jsonFile) throws IOException {
        if (!Files.isRegularFile(jsonFile)) {
            Files.createFile(jsonFile);
            Files.writeString(jsonFile, Util.GSON.toJson(new Config()));
            return null;
        }

        return Util.GSON.fromJson(Files.newBufferedReader(jsonFile), Config.class);
    }

}
