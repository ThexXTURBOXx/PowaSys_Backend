package de.femtopedia.powasysbackend.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SerialPort {

    private final int powadorId;
    private final String serialPort;

}
