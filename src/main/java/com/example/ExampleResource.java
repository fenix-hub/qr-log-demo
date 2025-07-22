package com.example;

import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("")
public class ExampleResource {

    @Inject
    Service service;

    @Path("/hello")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello, world!";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/qr")
    public Map<String, String> getQR(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) throws Exception {
        return service.getData(uriInfo.getRequestUri().toString());
    }

    @GET
    @Produces("image/png")
    @Path("/qr.png")
    public byte[] getQRBytes(@Context UriInfo uriInfo) throws Exception {
        return service.getData(uriInfo.getRequestUri().toString()).get("qr").getBytes(StandardCharsets.UTF_8);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tokens")
    public Multi<String> getChannels() {
        return service.getChannels();
    }
}
