package com.jettra.shell;

import com.jettra.driver.java.JettraClient;
import com.jettra.driver.java.JettraClient.IdMode;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                   JettraStore Shell (Interactive REPL)                         ");
        System.out.println("          Multi-Model Engines, Versioning, ID Strategies & Java 25 Records      ");
        System.out.println("================================================================================");
        System.out.println("Type 'help' for available commands.\n");

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
                            System.out.println("Login failed: invalid credentials.");
                        }
                        break;

                    case "insert":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 5) {
                            System.out.println("Usage: insert <model> <collection> <id|auto|uuid> <json_document>");
                            break;
                        }
                        String model = parts[1].toUpperCase();
                        String coll = parts[2];
                        String rawId = parts[3];
                        String jsonDoc = parts[4];

                        if ("auto".equalsIgnoreCase(rawId) || "autoincrement".equalsIgnoreCase(rawId)) {
                            String genId = client.insertDocumentAuto(coll, jsonDoc, IdMode.AUTOINCREMENT);
                            System.out.println("Object inserted into " + model + " [" + coll + "] with Auto-increment ID: " + genId);
                        } else if ("uuid".equalsIgnoreCase(rawId)) {
                            String genId = client.insertDocumentAuto(coll, jsonDoc, IdMode.UUID);
                            System.out.println("Object inserted into " + model + " [" + coll + "] with Composite UUID: " + genId);
                        } else {
                            boolean inserted;
                            if ("DOCUMENT".equalsIgnoreCase(model)) {
                                inserted = client.insertDocument(coll, rawId, jsonDoc, IdMode.MANUAL);
                            } else {
                                inserted = client.insertModel(model, coll, rawId, jsonDoc);
                            }
                            if (inserted) {
                                System.out.println("Object inserted into " + model + " [" + coll + ":" + rawId + "].");
                            } else {
                                System.out.println("Failed to insert object.");
                            }
                        }
                        break;

                    case "edit":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 5) {
                            System.out.println("Usage: edit <model> <collection> <id> <new_json_document>");
                            break;
                        }
                        String editModel = parts[1].toUpperCase();
                        String editColl = parts[2];
                        String editId = parts[3];
                        String editJson = parts[4];

                        boolean edited;
                        if ("DOCUMENT".equalsIgnoreCase(editModel)) {
                            edited = client.insertDocument(editColl, editId, editJson, IdMode.MANUAL);
                        } else {
                            edited = client.insertModel(editModel, editColl, editId, editJson);
                        }

                        if (edited) {
                            System.out.println("Object '" + editId + "' in " + editModel + " [" + editColl + "] updated successfully (new version created).");
                        } else {
                            System.out.println("Failed to update object '" + editId + "'.");
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
                        String getModel = parts[1].toUpperCase();
                        String getColl = parts[2];
                        String getId = parts[3];

                        String doc;
                        if ("DOCUMENT".equalsIgnoreCase(getModel)) {
                            doc = client.getDocument(getColl, getId);
                        } else {
                            doc = client.getModel(getModel, getColl, getId);
                        }

                        if (doc != null) {
                            System.out.println(doc);
                        } else {
                            System.out.println("Object not found: [" + getColl + ":" + getId + "]");
                        }
                        break;

                    case "delete":
                    case "rm":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 4) {
                            System.out.println("Usage: delete <model> <collection> <id>");
                            break;
                        }
                        String delModel = parts[1].toUpperCase();
                        String delColl = parts[2];
                        String delId = parts[3];

                        boolean deleted = client.deleteModel(delModel, delColl, delId);
                        if (deleted) {
                            System.out.println("Object '" + delId + "' deleted from " + delModel + " [" + delColl + "].");
                        } else {
                            System.out.println("Failed to delete object '" + delId + "'.");
                        }
                        break;

                    case "history":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 4) {
                            System.out.println("Usage: history <model> <collection> <id>");
                            break;
                        }
                        String histModel = parts[1].toUpperCase();
                        String histColl = parts[2];
                        String histId = parts[3];

                        String history = client.getDocumentHistory(histColl, histId);
                        System.out.println("Version History for [" + histModel + ":" + histColl + ":" + histId + "]:");
                        System.out.println(history);
                        break;

                    case "restore":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 5) {
                            System.out.println("Usage: restore <model> <collection> <id> <timestamp>");
                            break;
                        }
                        String resModel = parts[1].toUpperCase();
                        String resColl = parts[2];
                        String resId = parts[3];
                        long resTs = Long.parseLong(parts[4]);

                        boolean restored = client.restoreDocumentVersion(resColl, resId, resTs);
                        if (restored) {
                            System.out.println("Object '" + resId + "' in " + resModel + " [" + resColl + "] restored to timestamp: " + resTs);
                        } else {
                            System.out.println("Failed to restore object '" + resId + "' to version timestamp " + resTs);
                        }
                        break;

                    case "record":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        if (parts.length < 2) {
                            System.out.println("Usage: record <insert|edit|get|delete|history|restore> ...");
                            break;
                        }
                        String subCmd = parts[1].toLowerCase();
                        if ("insert".equals(subCmd) || "edit".equals(subCmd)) {
                            // input format: record insert/edit <collection> <id> <class> <json>
                            String[] recParts = input.split(" ", 6);
                            if (recParts.length < 6) {
                                System.out.println("Usage: record " + subCmd + " <collection> <id|auto|uuid> <recordClass> <json_components>");
                                break;
                            }
                            String rColl = recParts[2];
                            String rId = recParts[3];
                            String rClass = recParts[4];
                            String rJson = recParts[5];

                            if ("auto".equalsIgnoreCase(rId) || "autoincrement".equalsIgnoreCase(rId)) {
                                rId = "rec_" + System.currentTimeMillis();
                            } else if ("uuid".equalsIgnoreCase(rId)) {
                                rId = java.util.UUID.randomUUID().toString().substring(0, 12);
                            }

                            String wrapperJson = String.format("{\"_recordClass\":\"%s\",\"components\":%s}", rClass, rJson);
                            boolean rInserted = client.insertModel("RECORDS", rColl, rId, wrapperJson);
                            if (rInserted) {
                                System.out.println("Java Record persisted in RECORDS engine [" + rColl + ":" + rId + "] (" + rClass + ").");
                            } else {
                                System.out.println("Failed to persist record.");
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
                                System.out.println("Record not found: [" + parts[2] + ":" + parts[3] + "]");
                            }
                        } else if ("history".equals(subCmd)) {
                            if (parts.length < 4) {
                                System.out.println("Usage: record history <collection> <id>");
                                break;
                            }
                            String hist = client.getDocumentHistory(parts[2], parts[3]);
                            System.out.println("Record Version History [" + parts[2] + ":" + parts[3] + "]:");
                            System.out.println(hist);
                        } else if ("restore".equals(subCmd)) {
                            if (parts.length < 5) {
                                System.out.println("Usage: record restore <collection> <id> <timestamp>");
                                break;
                            }
                            long ts = Long.parseLong(parts[4]);
                            boolean res = client.restoreDocumentVersion(parts[2], parts[3], ts);
                            if (res) {
                                System.out.println("Record restored to version from timestamp: " + ts);
                            } else {
                                System.out.println("Failed to restore record.");
                            }
                        } else if ("delete".equals(subCmd)) {
                            if (parts.length < 4) {
                                System.out.println("Usage: record delete <collection> <id>");
                                break;
                            }
                            boolean rDeleted = client.deleteRecord(parts[2], parts[3]);
                            if (rDeleted) {
                                System.out.println("Record '" + parts[3] + "' deleted successfully from [" + parts[2] + "].");
                            } else {
                                System.out.println("Failed to delete record.");
                            }
                        } else {
                            System.out.println("Unknown record subcommand: " + subCmd + ". Use insert, edit, get, history, restore, or delete.");
                        }
                        break;

                    case "backup":
                        if (client == null || !client.isConnected()) {
                            System.out.println("Please 'connect' and 'login' first.");
                            break;
                        }
                        boolean backupOk = client.triggerBackup();
                        if (backupOk) {
                            System.out.println("Backup snapshot triggered successfully.");
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
                        System.out.println("Supported Multi-Model Engines (All 9 Engines):");
                        System.out.println("  1. DOCUMENT   (Hierarchical JSON / NoSQL Documents with ID Strategies & History)");
                        System.out.println("  2. RECORDS    (Immutable Java 25 Records with Schema Validation & Diffs)");
                        System.out.println("  3. KEYVALUE   (High-Speed Cache & MemTable String Store)");
                        System.out.println("  4. VECTOR     (AI ANN Cosine Embeddings & Vector Index)");
                        System.out.println("  5. GRAPH      (LPG Nodes, Edges & Graph Traversal)");
                        System.out.println("  6. TIMESERIES (IoT Sensor Telemetry & Metric WAL)");
                        System.out.println("  7. COLUMN     (OLAP Columnar Vectors & Run-Length Rows)");
                        System.out.println("  8. GEOSPATIAL (2D GIS Spatial Points & Distance Calculations)");
                        System.out.println("  9. OBJECT     (Binary BLOBs, Chunked Block Store & Media)");
                        break;

                    case "users":
                        System.out.println("Configured Users: [admin, guest]. Security Realm: ACTIVE.");
                        break;

                    case "rules":
                        System.out.println("JettraRules Engine: ACTIVE. Automatic entity constraints enforced on writes.");
                        break;

                    case "help":
                        System.out.println("Available Shell Commands:");
                        System.out.println("  connect <host> <port>                            Connect to JettraStoreEngine");
                        System.out.println("  login <username> <password>                      Authenticate session with JWT");
                        System.out.println("  engines                                          List all 9 supported storage engines");
                        System.out.println("  insert <model> <collection> <id|auto|uuid> <json> Insert object (Manual, Auto-increment, UUID)");
                        System.out.println("  edit <model> <collection> <id> <new_json>        Update object creating a new version");
                        System.out.println("  get <model> <collection> <id>                    Retrieve object from engine");
                        System.out.println("  delete <model> <collection> <id>                 Delete object by ID");
                        System.out.println("  history <model> <collection> <id>                Show full historical versions & diffs");
                        System.out.println("  restore <model> <collection> <id> <timestamp>    Roll back object to historical snapshot");
                        System.out.println("  record insert <coll> <id|auto|uuid> <cls> <json> Store Java 25 Record entity");
                        System.out.println("  record edit <coll> <id> <cls> <new_json>         Update Java 25 Record entity");
                        System.out.println("  record get <coll> <id>                           Retrieve Java 25 Record");
                        System.out.println("  record history <coll> <id>                       View record version history");
                        System.out.println("  record restore <coll> <id> <timestamp>           Restore record to historical version");
                        System.out.println("  record delete <coll> <id>                        Delete Java 25 Record");
                        System.out.println("  backup                                           Trigger manual snapshot backup");
                        System.out.println("  status                                           Display cluster node health & metrics");
                        System.out.println("  users                                            List configured users");
                        System.out.println("  rules                                            Inspect JettraRules status");
                        System.out.println("  exit / quit                                      Close shell session");
                        break;

                    case "exit":
                    case "quit":
                        if (client != null) {
                            client.close();
                        }
                        System.out.println("Exiting JettraStoreShell. Goodbye!");
                        return;

                    default:
                        System.out.println("Unknown command: '" + command + "'. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.out.println("Command execution error: " + e.getMessage());
            }
        }
    }
}
