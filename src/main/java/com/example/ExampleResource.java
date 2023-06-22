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
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @GET
    @Produces({"image/png", "image/jpeg", "image/gif"})
    @Path("/qr")
    public byte[] getTokenQR() {
        return service.getTokenQR();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/qr3")
    public Map<String, String> getTokenQR3() throws Exception {
        return service.getData();
    }

    @GET
    @Produces("image/png")
    @Path("/qr2")
    public Uni<byte[]> getTokenQR2(
            @QueryParam("token") String token
    ) {
        return Uni.createFrom().item(service.getQR(token));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tokens")
    public Multi<Service.Token> getTokens() {
        return service.getTokens();
    }
}
