package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

@ServerEndpoint("/{name}")
@ApplicationScoped
public class StartWebSocket {

    @Inject
    Service service;

    ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

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
    public void onOpen(Session session, @PathParam("name") String name) {

        service.getTokens().filter(
                (Service.Token token) -> {
                    return token.token().equals(name);
                }
        ).toUni()
                .subscribe().with(
                (Service.Token token) -> {
                    if (Objects.isNull(token)) {
                        System.out.println("onTokenConnectionOpening> " + name + ": token not found");
                        try {
                            session.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("onTokenConnectionOpened> " + name);
                        sessions.put(name, session.getOpenSessions());
                    }
                },
                (Throwable throwable) -> {
                    System.out.println("onTokenConnectionOpening> " + name + ": " + throwable);
                    try {
                        session.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {
        System.out.println("onClose> " + name);
        sessions.remove(name);
    }

    @OnError
    public void onError(Session session, @PathParam("name") String name, Throwable throwable) {
        System.out.println("onError> " + name + ": " + throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("name") String name) {
        service.getTokens().filter(
                (Service.Token token) -> {
                    return token.token().equals(name);
                }
        ).toUni().subscribe().with(
                (Service.Token token) -> {
                    System.out.println("onTokenMessage> " + name + ": " + message);
                    String username = message.split(":")[1];
                    broadcast(name, new ConnectionMessage("connected", username).toJSON());
                    closeChannel(name);
                }
        );
    }

    private void closeChannel(String channel) {
        sessions.get(channel).forEach(
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
        sessions.get(channel).forEach(
                (Session session) -> {
                    System.out.println("session> "  + session);
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
