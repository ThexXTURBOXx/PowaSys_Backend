package de.femtopedia.powasysbackend.rest;

import de.femtopedia.powasysbackend.util.Util;
import io.javalin.Javalin;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RestAPI {

    private static final Map<String, String> endpoints = Map.ofEntries(
            Map.entry("GET /discovery", "Shows this page, which contains information about all available endpoints."),
            Map.entry("GET /get/:id", "Returns the entry with the given :id from the database.")
    );

    private final Javalin javalin;

    public RestAPI() {
        this(Javalin.create()
                .get("/discovery", ctx -> ctx.result(Util.GSON.toJson(endpoints)))
                .get("/get/:id", ctx -> ctx.result("it works!")) // TODO
        );
    }

    public RestAPI start() {
        javalin.start();
        return this;
    }

    public RestAPI start(int port) {
        javalin.start(port);
        return this;
    }

    public RestAPI start(String host, int port) {
        javalin.start(host, port);
        return this;
    }

    public void shutdown() {
        javalin.close();
    }

}
