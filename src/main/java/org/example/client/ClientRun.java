package org.example.client;

import java.util.Scanner;


public class ClientRun {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        String username = scanner.nextLine();

        User user = new User(username);

        System.out.println("Write your first message.");

        user.sendMessage();
    }
}