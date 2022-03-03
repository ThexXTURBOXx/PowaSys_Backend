package de.femtopedia.powasysbackend.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class Util {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXXXXX");

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(OffsetDateTime.class,
                    (JsonDeserializer<OffsetDateTime>) (json, type, ctx) -> OffsetDateTime.parse(json.getAsJsonPrimitive().getAsString(), FORMATTER))
            .registerTypeAdapter(OffsetDateTime.class,
                    (JsonSerializer<OffsetDateTime>) (obj, type, ctx) -> new JsonPrimitive(FORMATTER.format(obj)))
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
