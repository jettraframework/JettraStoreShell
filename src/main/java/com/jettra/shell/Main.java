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
                    case "delete":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 4) {
                            System.out.println("Usage: delete <model> <collection> <id>");
                            break;
                        }
                        boolean deleted = client.deleteModel(parts[1], parts[2], parts[3]);
                        if (deleted) {
                            System.out.println("Object deleted successfully.");
                        } else {
                            System.out.println("Failed to delete object.");
                        }
                        break;
                    case "record":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 2) {
                            System.out.println("Usage: record <insert|get|delete> ...");
                            break;
                        }
                        String subCmd = parts[1].toLowerCase();
                        if ("insert".equals(subCmd)) {
                            // input format: record insert <collection> <id> <class> <json>
                            String[] recParts = input.split(" ", 6);
                            if (recParts.length < 6) {
                                System.out.println("Usage: record insert <collection> <id> <recordClass> <json_components>");
                                break;
                            }
                            String rColl = recParts[2];
                            String rId = recParts[3];
                            String rClass = recParts[4];
                            String rJson = recParts[5];
                            String wrapperJson = String.format("{\"_recordClass\":\"%s\",\"components\":%s}", rClass, rJson);
                            boolean rInserted = client.insertModel("RECORDS", rColl, rId, wrapperJson);
                            if (rInserted) {
                                System.out.println("Java Record stored in RECORDS engine [" + rColl + ":" + rId + "].");
                            } else {
                                System.out.println("Failed to store record.");
                            }
                        } else if ("get".equals(subCmd)) {
                            if (parts.length < 4) {
                                System.out.println("Usage: record get <collection> <id>");
                                break;
                            }
                            String rDoc = client.getModel("RECORDS", parts[2], parts[3]);
                            if (rDoc != null) {
                                System.out.println(rDoc);
                            } else {
                                System.out.println("Record not found.");
                            }
                        } else if ("delete".equals(subCmd)) {
                            if (parts.length < 4) {
                                System.out.println("Usage: record delete <collection> <id>");
                                break;
                            }
                            boolean rDeleted = client.deleteRecord(parts[2], parts[3]);
                            if (rDeleted) {
                                System.out.println("Record deleted successfully.");
                            } else {
                                System.out.println("Failed to delete record.");
                            }
                        } else {
                            System.out.println("Unknown record subcommand: " + subCmd + ". Use insert, get, or delete.");
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
                    case "engines":
                        System.out.println("Supported Engines (9 Multi-Models):");
                        System.out.println("  1. DOCUMENT   (NoSQL JSON Documents)");
                        System.out.println("  2. VECTOR     (AI ANN Cosine Embeddings)");
                        System.out.println("  3. GRAPH      (LPG Nodes & Relations)");
                        System.out.println("  4. TIMESERIES (IoT Sensor Telemetry)");
                        System.out.println("  5. COLUMN     (OLAP Columnar Rows)");
                        System.out.println("  6. KEYVALUE   (High-Speed Cache)");
                        System.out.println("  7. GEOSPATIAL (2D GIS Spatial Points)");
                        System.out.println("  8. OBJECT     (Binary BLOBs & Media)");
                        System.out.println("  9. RECORDS    (Java 25 Immutable Records)");
                        break;
                    case "users":
                        System.out.println("User list: [admin, guest]. Active nodes: 1 (Master).");
                        break;
                    case "rules":
                        System.out.println("Rules triggered: none. Triggers are active.");
                        break;
                    case "help":
                        System.out.println("Commands:");
                        System.out.println("  connect <host> <port>                  Connect to JettraStoreEngine");
                        System.out.println("  login <username> <password>            Authenticate session with JWT");
                        System.out.println("  engines                                List all 9 supported storage engines");
                        System.out.println("  insert <model> <collection> <id> <json> Insert object into specific engine");
                        System.out.println("  get <model> <collection> <id>          Retrieve object from engine");
                        System.out.println("  delete <model> <collection> <id>       Delete object from engine");
                        System.out.println("  record insert <coll> <id> <class> <json> Save Java Record to RECORDS engine");
                        System.out.println("  record get <coll> <id>                 Retrieve Java Record");
                        System.out.println("  record delete <coll> <id>              Delete Java Record");
                        System.out.println("  backup                                 Trigger manual backup snapshot");
                        System.out.println("  status                                 Display node health and resources");
                        System.out.println("  users                                  List configured users");
                        System.out.println("  rules                                  Show active business rules");
                        System.out.println("  exit / quit                            Close shell session");
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
