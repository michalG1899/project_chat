package org.example.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

public class User {

    private final String username;
    private final WebSocket.Listener webSocketListener;
    private WebSocket webSocket;


    public User(String username) {
        this.username = username;

        webSocketListener = new WebSocket.Listener() {
            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                error.printStackTrace();
                WebSocket.Listener.super.onError(webSocket, error);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                System.out.println(reason);
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                System.out.println((String) data);
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onOpen(WebSocket webSocket) {
                System.out.println("Welcome user: " + username + "!");
                WebSocket.Listener.super.onOpen(webSocket);
            }
        };

        HttpClient httpClient = HttpClient.newHttpClient();
        WebSocket.Builder webSocketBuilder = httpClient.newWebSocketBuilder();

        webSocket = webSocketBuilder
                .buildAsync(URI.create("ws://localhost:8080/chat"), webSocketListener)
                .join();

        Runtime.getRuntime().addShutdownHook(getShutdownHook(webSocket));
    }

    private static Thread getShutdownHook(WebSocket webSocket) {
        var shutdownHook = Thread.ofPlatform()
                .unstarted(() -> webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Exiting").join());
        return shutdownHook;
    }

    public void sendMessage() {
        Scanner scanner = new Scanner(System.in);
        while (!webSocket.isOutputClosed()) {
            webSocket.sendText(username + ": " + scanner.nextLine(), false).join();
        }
    }

}
