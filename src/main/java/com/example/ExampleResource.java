package com.example;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.Map;

import static io.quarkus.devui.runtime.comms.MessageType.Response;

@Path("")
public class ExampleResource {

    @Inject
    Service service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/qr")
    public Map<String, String> getTokenQR3() throws Exception {
        return service.getData();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tokens")
    public Multi<Service.Token> getTokens() {
        return service.getTokens();
    }
}
