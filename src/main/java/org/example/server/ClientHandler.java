package org.example.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;


    public ClientHandler(Socket socket) throws IOException, NoSuchAlgorithmException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();

        validateWebsocketConnection(in, out);
    }


    private static void readResponse(InputStream in) throws IOException {
        int opcode = in.read(); // it should always be 0x1 (for now)
        int messageLength = in.read() - 128;
        messageLength = switch (messageLength) {
            case 127 -> throw new RuntimeException("Very long message!");
            case 126 -> throw new RuntimeException("Longer message!");
            default -> messageLength;
        };
        byte[] decodingKey = new byte[]{(byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read()};
        byte[] encodedMessage = in.readNBytes(messageLength);
        var decodedMessage = new String(decodeBytes(encodedMessage, decodingKey));
        System.out.println(decodedMessage);
        if (decodedMessage.contains("\u0003�Exiting")) {
            System.out.println("Client has disconnected!");
            System.exit(0);
        }
    }

    private static byte[] decodeBytes(byte[] encodedMessage, byte[] decodingKey) {
        byte[] decoded = new byte[encodedMessage.length];
        for (int i = 0; i < encodedMessage.length; i++) {
            decoded[i] = (byte) (encodedMessage[i] ^ decodingKey[i & 0x3]);
        }
        return decoded;
    }

    private static void validateWebsocketConnection(InputStream inputStream, OutputStream outputStream) throws IOException, NoSuchAlgorithmException { // dokument z mozilli
        Scanner s = new Scanner(inputStream, StandardCharsets.UTF_8);
        String data = s.useDelimiter("\\r\\n\\r\\n").next();
        Matcher get = Pattern.compile("^GET").matcher(data);
        if (get.find()) {
            handshake(data, outputStream);
        } else {
            throw new RuntimeException("Not a proper websocket protocol!");
        }
    }

    private static void handshake(String data, OutputStream out) throws IOException, NoSuchAlgorithmException { //handshake dla każdego klienta
        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
        match.find();
        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                + "Connection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: "
                + encodeHashAndBase64(match)
                + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        out.write(response, 0, response.length);
    }

    private static String encodeHashAndBase64(Matcher match) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-1")
                .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                        .getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    @Override
    public void run() {
        System.out.println("A client connected!");
        while (true) {
                try {
                    readResponse(in);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
    }
}

