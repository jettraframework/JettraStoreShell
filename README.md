# JettraStoreShell (REPL CLI)

Interactive CLI Shell for **JettraStoreEngine** with multi-model support across all 9 database engines, including native **Java 25 Records**.

## Build and Run

```bash
mvn clean package -DskipTests
java -jar target/JettraStoreShell-1.0-SNAPSHOT.jar
```

## Commands Overview

```text
==================================================
              JettraStore Shell                   
==================================================
Type 'help' for commands.

jettra> help
Commands:
  connect <host> <port>                  Connect to JettraStoreEngine
  login <username> <password>            Authenticate session with JWT
  engines                                List all 9 supported storage engines
  insert <model> <collection> <id> <json> Insert object into specific engine
  get <model> <collection> <id>          Retrieve object from engine
  delete <model> <collection> <id>       Delete object from engine
  record insert <coll> <id> <class> <json> Save Java Record to RECORDS engine
  record get <coll> <id>                 Retrieve Java Record
  record delete <coll> <id>              Delete Java Record
  backup                                 Trigger manual backup snapshot
  status                                 Display node health and resources
  users                                  List configured users
  rules                                  Show active business rules
  exit / quit                            Close shell session
```

## Example Session with Records Engine

```text
jettra> connect localhost 8086
Connected to JettraStoreEngine at localhost:8086

jettra> login admin admin
Login successful.

jettra> engines
Supported Engines (9 Multi-Models):
  1. DOCUMENT   (NoSQL JSON Documents)
  2. VECTOR     (AI ANN Cosine Embeddings)
  3. GRAPH      (LPG Nodes & Relations)
  4. TIMESERIES (IoT Sensor Telemetry)
  5. COLUMN     (OLAP Columnar Rows)
  6. KEYVALUE   (High-Speed Cache)
  7. GEOSPATIAL (2D GIS Spatial Points)
  8. OBJECT     (Binary BLOBs & Media)
  9. RECORDS    (Java 25 Immutable Records)

jettra> record insert employees emp_101 com.enterprise.model.EmployeeRecord {"id":"emp_101","name":"Carlos Mendez","salary":85000}
Java Record stored in RECORDS engine [employees:emp_101].

jettra> record get employees emp_101
{"_recordClass":"com.enterprise.model.EmployeeRecord","_timestamp":1755735492000,"_version":1,"_schema":{"id":"String","name":"String","salary":"Double"},"components":{"id":"emp_101","name":"Carlos Mendez","salary":85000}}

jettra> record delete employees emp_101
Record deleted successfully.

jettra> exit
Goodbye!
```
