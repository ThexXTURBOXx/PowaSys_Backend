package de.femtopedia.powasysbackend.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Powador {

    private final int powadorId;
    private final String name;
    private final String color;

}
