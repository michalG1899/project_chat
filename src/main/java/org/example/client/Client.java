package org.example.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

public class Client implements Runnable {

    private final String username;

    public Client(String username) {
        this.username = username;
    }

    @Override
    public void run() {
        var webSocketListener = new WebSocket.Listener() {

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                System.out.println(data);
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onOpen(WebSocket webSocket) {
                System.out.println("Welcome user: " + username + "!");
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                System.out.println("Connection closed. Reason: " + reason);
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }
        };

        HttpClient client = HttpClient.newHttpClient();
        WebSocket.Builder webSocketBuilder = client.newWebSocketBuilder();
        WebSocket webSocket =
                webSocketBuilder.buildAsync(URI.create("ws://localhost:8080/chat"), webSocketListener).join(); //definiowanie socketa, do którego chcemy się połączyć (zbudowanie klienta websocketowego, podłączenie się pod serwer)

        var shutdownHook = Thread.ofPlatform()
                .unstarted(() -> webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Exiting").join());
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Napisz wiadomosc:");

        while (true) {
            String input = scanner.nextLine();
            webSocket.sendText(username + ": " + input, false).join();
        }
    }
}

