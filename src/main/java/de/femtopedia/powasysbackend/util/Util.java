package de.femtopedia.powasysbackend.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Optional;

public final class Util {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public static Optional<Double> parseDouble(String toParse) {
        try {
            return Optional.of(Double.parseDouble(toParse));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseInt(String toParse) {
        try {
            return Optional.of(Integer.parseInt(toParse));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static boolean isBlank(String string) {
        return string == null || string.isBlank();
    }

    private Util() {
        throw new UnsupportedOperationException();
    }

}
