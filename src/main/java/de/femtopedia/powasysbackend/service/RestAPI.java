package de.femtopedia.powasysbackend.service;

import de.femtopedia.powasysbackend.api.Powador;
import de.femtopedia.powasysbackend.api.Storage;
import de.femtopedia.powasysbackend.util.Util;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import java.util.List;
import java.util.Map;

public record RestAPI(Javalin javalin, Storage storage, List<Powador> powadors)
        implements AutoCloseable {

    private static final Map<String, String> endpoints = Map.ofEntries(
            Map.entry("GET /discovery", "Shows this page, which contains information about all available endpoints."),
            Map.entry("GET /powas", "Returns all Powador IDs and their names."),
            Map.entry("GET /get/{id}", "Returns the entry with the given {id} from the database."),
            Map.entry("GET /24h", "Returns all entries from within the last 24 hours from the database.")
    );

    public RestAPI(Storage storage, List<Powador> powadors) {
        this(Javalin.create(config -> config.bundledPlugins.enableCors(
                                container -> container.addRule(CorsPluginConfig.CorsRule::anyHost)))
                        .get("/discovery", ctx -> ctx.result(Util.GSON.toJson(endpoints)))
                        .get("/powas", ctx -> ctx.result(Util.GSON.toJson(powadors)))
                        .get("/get/{id}", ctx -> ctx.result(Util.GSON.toJson(
                                storage.getEntry(ctx.pathParamAsClass("id", Integer.class).get()))))
                        .get("/24h", ctx -> ctx.result(Util.GSON.toJson(storage.getLast24h(
                                ctx.queryParamAsClass("minDiv", Double.class).getOrDefault(0.0)
                        )))),
                storage, powadors);
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

    @Override
    public void close() {
        javalin.stop();
    }

}
