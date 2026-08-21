# JettraStoreShell - Comprehensive Guide & Architecture Manual

## 1. Overview & Architecture
`JettraStoreShell` is an interactive Command Line Interface (REPL) for **JettraStoreEngine**. It enables database administrators and developers to inspect nodes, trigger backups, manage security roles, and perform CRUD operations across all 9 multi-model database engines.

```
                    JettraStoreShell (REPL)
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
    Administrative Commands              Multi-Model CRUD
    - connect, login, status             - document, vector, graph
    - users, rules, backup, engines      - timeseries, column, kv, geo
                                         - record (Java 25 Records)
```

---

## 2. Key Features
- **Dedicated Java 25 Record Commands**: `record insert`, `record get`, `record delete`.
- **Multi-Model Support**: CRUD access to `DOCUMENT`, `VECTOR`, `GRAPH`, `TIMESERIES`, `COLUMN`, `KEYVALUE`, `GEOSPATIAL`, `OBJECT`, and `RECORDS`.
- **Administrative Utilities**: Cluster status inspection, manual backup triggering, and role-based user management.
- **Pure Java 25 Runtime**: Lightweight, fast startup, bundled with shaded fat JAR.

---

## 3. Installation & Launch
```bash
mvn clean package -DskipTests
java -jar target/JettraStoreShell-1.0-SNAPSHOT.jar
```

---

## 4. Commands Reference & Examples

### 4.1 Connection & Session Management
```text
jettra> connect localhost 8086
Connected to JettraStoreEngine at localhost:8086

jettra> login admin admin
Login successful.

jettra> status
{
  "ram_usage": "256 MB / 4096 MB",
  "disk_usage": "1.2 GB / 500 GB",
  "nodes": "1 (Master)",
  "network": "ONLINE"
}

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
```

### 4.2 Records Engine Commands
```text
# Insert a typed Java Record
jettra> record insert employees emp_101 com.enterprise.model.EmployeeRecord {"id":"emp_101","fullName":"Carlos Mendez","salary":95000.0}
Java Record stored in RECORDS engine [employees:emp_101].

# Retrieve Record
jettra> record get employees emp_101
{"_recordClass":"com.enterprise.model.EmployeeRecord","_timestamp":1755735492000,"_version":1,"_schema":{"id":"String","fullName":"String","salary":"Double"},"components":{"id":"emp_101","fullName":"Carlos Mendez","salary":95000.0}}

# Delete Record
jettra> record delete employees emp_101
Record deleted successfully.
```

### 4.3 Multi-Model Generic Insert / Get / Delete
```text
# Document
jettra> insert document orders ord_1 {"customer":"Alice","total":250.0}
jettra> get document orders ord_1
jettra> delete document orders ord_1

# KeyValue
jettra> insert keyvalue config rate_limit "1000/min"
jettra> get keyvalue config rate_limit

# Vector
jettra> insert vector catalog item_vec {"vector":[0.12, 0.45, 0.88], "label":"shoes"}
jettra> get vector catalog item_vec
```

### 4.4 Maintenance Commands
```text
# Trigger cluster snapshot backup
jettra> backup
Backup triggered successfully.

# List active users
jettra> users
User list: [admin, guest]. Active nodes: 1 (Master).

# Exit session
jettra> exit
Goodbye!
```
