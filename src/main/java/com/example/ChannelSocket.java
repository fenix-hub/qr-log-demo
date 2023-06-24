package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

@ServerEndpoint("/{channel}")
@ApplicationScoped
public class ChannelSocket {

    @Inject
    Service service;

    ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    public record ConnectionMessage(String status, String username) {
        static ObjectMapper objectMapper = new ObjectMapper();
        public String toJSON() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("channel") String channel) {
        service.getChannels().filter(
                (String activeChannel) -> {
                    return activeChannel.equals(channel);
                }
        ).toUni()
                .subscribe().with(
                    (String activeChannel) -> {
                        System.out.println("onOpen?> " + activeChannel);
                        if (Objects.isNull(activeChannel)) {
                            try {
                                session.close();
                            } catch (IOException e) {
                                // ignore
                            }
                        } else {
                            sessions.put(channel, session);
                        }
                    }
        );
    }

    @OnClose
    public void onClose(Session session, @PathParam("channel") String channel) {
        sessions.remove(channel);
    }

    @OnError
    public void onError(Session session, @PathParam("channel") String channel, Throwable throwable) {
    }

    @OnMessage
    public void onMessage(String message, @PathParam("channel") String channel) {
        service.getChannels().filter(
                (String activeChannel) -> {
                    return activeChannel.equals(channel);
                }
        ).toUni().subscribe().with(
                (String activeChannel) -> {
                    String username = message.split(":")[1];
                    broadcast(channel, new ConnectionMessage("connected", username).toJSON());
                    closeChannel(channel);
                }
        );
    }

    private void closeChannel(String channel) {
        sessions.get(channel).getOpenSessions().forEach(
                (Session session) -> {
                    try {
                        session.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void broadcast(String channel, String message) {
        sessions.get(channel).getOpenSessions().forEach(
                (Session session) -> {
                    session.getAsyncRemote().sendObject(
                            requireNonNull(message),
                            result -> {
                                if (result.getException() != null) {
                                    System.out.println("Unable to send message: " + result.getException());
                                }
                            }
                    );
                }
        );
    }
}
