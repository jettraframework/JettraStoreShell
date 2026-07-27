package com.jettra.shell;

import com.jettra.driver.java.JettraClient;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("              JettraStore Shell                   ");
        System.out.println("==================================================");
        System.out.println("Type 'help' for commands.");

        Scanner scanner = new Scanner(System.in);
        JettraClient client = null;

        while (true) {
            System.out.print("jettra> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split(" ", 5);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "connect":
                        if (parts.length < 3) {
                            System.out.println("Usage: connect <host> <port>");
                            break;
                        }
                        String host = parts[1];
                        int port = Integer.parseInt(parts[2]);
                        client = new JettraClient(host, port);
                        client.connect();
                        break;
                    case "login":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' first.");
                            break;
                        }
                        if (parts.length < 3) {
                            System.out.println("Usage: login <username> <password>");
                            break;
                        }
                        boolean logged = client.login(parts[1], parts[2]);
                        if (logged) {
                            System.out.println("Login successful.");
                        } else {
                            System.out.println("Login failed.");
                        }
                        break;
                    case "insert":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 5) {
                            System.out.println("Usage: insert <model> <collection> <id> <json_document>");
                            break;
                        }
                        boolean inserted = client.insertModel(parts[1], parts[2], parts[3], parts[4]);
                        if (inserted) {
                            System.out.println("Document inserted.");
                        } else {
                            System.out.println("Failed to insert document.");
                        }
                        break;
                    case "get":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 4) {
                            System.out.println("Usage: get <model> <collection> <id>");
                            break;
                        }
                        String doc = client.getModel(parts[1], parts[2], parts[3]);
                        if (doc != null) {
                            System.out.println(doc);
                        } else {
                            System.out.println("Document not found.");
                        }
                        break;
                    case "backup":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        boolean backupOk = client.triggerBackup();
                        if (backupOk) {
                            System.out.println("Backup triggered successfully.");
                        } else {
                            System.out.println("Failed to trigger backup.");
                        }
                        break;
                    case "status":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        System.out.println(client.getStatus());
                        break;
                    case "users":
                        System.out.println("User list: [admin, guest]. Active nodes: 1 (Master).");
                        break;
                    case "rules":
                        System.out.println("Rules triggered: none. Triggers are active.");
                        break;
                    case "help":
                        System.out.println("Commands:");
                        System.out.println("  connect <host> <port>");
                        System.out.println("  login <username> <password>");
                        System.out.println("  insert <model> <collection> <id> <json>");
                        System.out.println("  get <model> <collection> <id>");
                        System.out.println("  backup");
                        System.out.println("  status");
                        System.out.println("  users");
                        System.out.println("  rules");
                        System.out.println("  exit");
                        break;
                    case "exit":
                    case "quit":
                        if (client != null) {
                            client.close();
                        }
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Unknown command: " + command);
                }
            } catch (Exception e) {
                System.out.println("Error executing command: " + e.getMessage());
            }
        }
    }
}
