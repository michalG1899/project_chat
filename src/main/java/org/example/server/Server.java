package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws IOException {
        new Server().startServer();
    }

    public void startServer() throws IOException {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
        ServerSocket server = new ServerSocket(8080);

        Thread serverSocketHandler = new Thread(() -> {
            try (server) {
                System.out.println("""
                        Server has started on 127.0.0.1:8080
                        Waiting for a connection…
                        """);
                while (!server.isClosed()) {
                try  {
                    Socket socket = server.accept();
                    clientProcessingPool.submit(new ClientHandler(socket));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        serverSocketHandler.start();
    }
}