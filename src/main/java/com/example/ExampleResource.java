package com.example;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("")
public class ExampleResource {

    @Inject
    Service service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/qr")
    public Map<String, String> getQR() throws Exception {
        return service.getData();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tokens")
    public Multi<String> getChannels() {
        return service.getChannels();
    }
}
