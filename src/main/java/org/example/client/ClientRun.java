package org.example.client;

import java.util.Scanner;


public class ClientRun {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        String username = scanner.nextLine();

        Client client = new Client(username);
        new Thread(client).start();

    }
}