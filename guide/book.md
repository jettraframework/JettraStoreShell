# JettraStoreShell - The Definitive Guide & Operational Manual

**JettraStoreShell** is the native, high-performance interactive Command-Line Interface (REPL) and administrative shell for **JettraStoreEngine**, engineered in Java 25. It enables database administrators, site reliability engineers, and software architects to interact with all 9 multi-model database engines, inspect Raft consensus cluster nodes, execute ACID transactions, and manage per-database RBAC security realms directly from the terminal.

---

# Table of Contents
- [Chapter 1: Architecture & REPL Lifecycle](#chapter-1-architecture--repl-lifecycle)
  - [1.1 Non-blocking Connection Architecture](#11-non-blocking-connection-architecture)
  - [1.2 Command Dispatcher & Tokenizer](#12-command-dispatcher--tokenizer)
- [Chapter 2: Installation, Build & Configuration](#chapter-2-installation-build--configuration)
  - [2.1 Building from Source](#21-building-from-source)
  - [2.2 Launching and Environment Variables](#22-launching-and-environment-variables)
- [Chapter 3: Authentication, Session Management & RBAC](#chapter-3-authentication-session-management--rbac)
  - [3.1 Cluster Node Connection](#31-cluster-node-connection)
  - [3.2 JWT Token Negotiation](#32-jwt-token-negotiation)
  - [3.3 Role-Based Permissions](#33-role-based-permissions)
- [Chapter 4: The 9 Multi-Model Database Engines in the Shell](#chapter-4-the-9-multi-model-database-engines-in-the-shell)
  - [4.1 Records Engine (Java 25 Records)](#41-records-engine-java-25-records)
  - [4.2 Document Engine (JSON / NoSQL)](#42-document-engine-json--nosql)
  - [4.3 Vector Engine (AI Embeddings)](#43-vector-engine-ai-embeddings)
  - [4.4 Graph Engine (Nodes & Relations)](#44-graph-engine-nodes--relations)
  - [4.5 TimeSeries Engine (Metrics & Telemetry)](#45-timeseries-engine-metrics--telemetry)
  - [4.6 Columnar Engine (OLAP Tables)](#46-columnar-engine-olap-tables)
  - [4.7 KeyValue Engine (Memory Cache)](#47-keyvalue-engine-memory-cache)
  - [4.8 Geospatial Engine (2D GIS Spatial Points)](#48-geospatial-engine-2d-gis-spatial-points)
  - [4.9 Object Engine (Binary BLOBs & Media)](#49-object-engine-binary-blobs--media)
- [Chapter 5: Dedicated Records Engine Command Suite](#chapter-5-dedicated-records-engine-command-suite)
  - [5.1 `record insert`](#51-record-insert)
  - [5.2 `record get`](#52-record-get)
  - [5.3 `record delete`](#53-record-delete)
- [Chapter 6: Cluster Administration, Backups & Monitoring](#chapter-6-cluster-administration-backups--monitoring)
  - [6.1 Node Health & Status](#61-node-health--status)
  - [6.2 Snapshot Backups](#62-snapshot-backups)
  - [6.3 User Management](#63-user-management)
- [Chapter 7: Interactive Scripting & Batch Execution](#chapter-7-interactive-scripting--batch-execution)

---

# Chapter 1: Architecture & REPL Lifecycle

```
                           User Terminal
                                 │
                   ┌─────────────┴─────────────┐
                   ▼                           ▼
            Standard Input             Terminal Output
                   │                           ▲
                   ▼                           │
       ┌──────────────────────────────────────────────┐
       │             JettraStoreShell (REPL)          │
       │  - Command Parser & Syntax Validator         │
       │  - JWT Session & Token Storage               │
       │  - JettraStoreDriverJava Client Core         │
       └──────────────────────────────────────────────┘
                               │
               HTTP / REST API (Port 8086)
                               │
                               ▼
       ┌──────────────────────────────────────────────┐
       │             JettraStoreEngine Node           │
       │  - 9 Multi-Model Database Engines            │
       │  - LSM-Tree / B-Tree Hybrid Storage Core     │
       │  - Raft Quorum Distributed Consensus         │
       └──────────────────────────────────────────────┘
```

### 1.1 Non-blocking Connection Architecture
`JettraStoreShell` utilizes the shaded `JettraStoreDriverJava` HTTP client, running on Java 25 Virtual Threads. It connects to the cluster REST port (default `8086`), authenticates using HMAC-SHA256 tokens (`JettraJWT`), and maintains session credentials transparently across command invocations.

### 1.2 Command Dispatcher & Tokenizer
The shell loop parses commands using delimiter-aware tokenization:
- Command token matching (`insert`, `get`, `delete`, `record`, `engines`, `status`, `backup`, `users`, `rules`, `help`, `exit`).
- Model type validation (`RECORDS`, `DOCUMENT`, `VECTOR`, `GRAPH`, `TIMESERIES`, `COLUMN`, `KEYVALUE`, `GEOSPATIAL`, `OBJECT`).
- JSON payload parsing with escape sequence sanitization.

---

# Chapter 2: Installation, Build & Configuration

### 2.1 Building from Source
`JettraStoreShell` is built with Apache Maven:

```bash
cd JettraStoreShell
mvn clean package -DskipTests
```

The build produces a single standalone executable uber-JAR in `target/JettraStoreShell-1.0-SNAPSHOT.jar`.

### 2.2 Launching and Environment Variables
```bash
java --enable-preview -jar target/JettraStoreShell-1.0-SNAPSHOT.jar
```

Optional environment variables:
- `JETTRA_HOST`: Default node host (e.g. `127.0.0.1`).
- `JETTRA_PORT`: Default REST port (e.g. `8086`).

---

# Chapter 3: Authentication, Session Management & RBAC

### 3.1 Cluster Node Connection
Establish an HTTP channel to a running JettraStoreEngine instance:
```text
jettra> connect localhost 8086
Connected to JettraStoreEngine at localhost:8086
```

### 3.2 JWT Token Negotiation
Authenticate your user credentials:
```text
jettra> login admin admin
Login successful.
```

Upon successful authentication, the shell stores the JWT bearer token in memory and attaches it automatically to the `Authorization: Bearer <token>` header of all subsequent database requests.

### 3.3 Role-Based Permissions
Privileges are enforced on a per-database basis:
- **`DB_ADMIN`**: Full DDL/DML on assigned database scope.
- **`READ_WRITE`**: Insert, update, and query entities.
- **`READ_ONLY`**: Search and get operations only.
- **`MANAGER`**: Trigger backups, restore points, and node health checks.

---

# Chapter 4: The 9 Multi-Model Database Engines in the Shell

`JettraStoreShell` provides access to all 9 multi-model database engines:

```text
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

### 4.1 Records Engine (Java 25 Records)
Direct storage of typed records with schema reflection:
```text
jettra> record insert employees emp_901 com.enterprise.model.EmployeeRecord {"id":"emp_901","fullName":"Carlos Mendez","salary":95000.0}
```

### 4.2 Document Engine (JSON / NoSQL)
Hierarchical NoSQL JSON documents:
```text
jettra> insert document invoices inv_1001 {"customer":"Acme Corp","total":1540.50,"status":"PAID"}
jettra> get document invoices inv_1001
```

### 4.3 Vector Engine (AI Embeddings)
AI embeddings with cosine distance search:
```text
jettra> insert vector product_embeddings sku_881 {"vector":[0.14, 0.88, 0.32, 0.55],"label":"running_shoes"}
jettra> get vector product_embeddings sku_881
```

### 4.4 Graph Engine (Nodes & Relations)
LPG nodes and edges for knowledge graphs:
```text
jettra> insert graph knowledge_graph node_carlos {"name":"Carlos Mendez","department":"Engineering"}
jettra> get graph knowledge_graph node_carlos
```

### 4.5 TimeSeries Engine (Metrics & Telemetry)
IoT temporal metric points:
```text
jettra> insert timeseries server_telemetry 1755735000000 {"cpu_load":42.5,"mem_mb":1840}
jettra> get timeseries server_telemetry 1755735000000
```

### 4.6 Columnar Engine (OLAP Tables)
Wide-column tabular projections:
```text
jettra> insert column sales_olap row_2026_q1 {"revenue":450000,"units":1200,"region":"LATAM"}
jettra> get column sales_olap row_2026_q1
```

### 4.7 KeyValue Engine (Memory Cache)
Ultra-low latency in-memory string values:
```text
jettra> insert keyvalue session_tokens sess_9901 "token_jwt_xyz_881"
jettra> get keyvalue session_tokens sess_9901
```

### 4.8 Geospatial Engine (2D GIS Spatial Points)
2D coordinates and GPS points:
```text
jettra> insert geospatial store_locations loc_panama {"name":"Panama Flagship","lat":8.9824,"lon":-79.5199}
jettra> get geospatial store_locations loc_panama
```

### 4.9 Object Engine (Binary BLOBs & Media)
Chunked binary BLOBs and Base64 payloads:
```text
jettra> insert object media_bucket invoice_pdf {"contentType":"application/pdf","size":4096,"base64":"JVBERi0xLjQK..."}
jettra> get object media_bucket invoice_pdf
```

---

# Chapter 5: Dedicated Records Engine Command Suite

`JettraStoreShell` includes first-class subcommands designed specifically for the **`RECORDS`** engine.

### 5.1 `record insert`
**Syntax:**
```text
record insert <collection> <id> <recordClass> <json_components>
```

**Example:**
```text
jettra> record insert employees emp_101 com.enterprise.model.EmployeeRecord {"id":"emp_101","fullName":"Carlos Mendez","department":"Engineering","salary":95000.0,"active":true}
Java Record stored in RECORDS engine [employees:emp_101].
```

### 5.2 `record get`
**Syntax:**
```text
record get <collection> <id>
```

**Example:**
```text
jettra> record get employees emp_101
{
  "_recordClass": "com.enterprise.model.EmployeeRecord",
  "_timestamp": 1755735492000,
  "_version": 1,
  "_schema": {
    "id": "String",
    "fullName": "String",
    "department": "String",
    "salary": "Double",
    "active": "Boolean"
  },
  "components": {
    "id": "emp_101",
    "fullName": "Carlos Mendez",
    "department": "Engineering",
    "salary": 95000.0,
    "active": true
  }
}
```

### 5.3 `record delete`
**Syntax:**
```text
record delete <collection> <id>
```

**Example:**
```text
jettra> record delete employees emp_101
Record deleted successfully.
```

---

# Chapter 6: Cluster Administration, Backups & Monitoring

### 6.1 Node Health & Status
```text
jettra> status
{
  "ram_usage": "256 MB / 4096 MB",
  "disk_usage": "1.2 GB / 500 GB",
  "nodes": "1 (Master)",
  "network": "ONLINE"
}
```

### 6.2 Snapshot Backups
Creates a point-in-time ZIP snapshot of all `.wal`, `.sst`, `.jettra`, and metadata files:
```text
jettra> backup
Backup triggered successfully.
```

### 6.3 User Management
```text
jettra> users
User list: [admin, guest]. Active nodes: 1 (Master).
```

---

# Chapter 7: Interactive Scripting & Batch Execution

You can pipe commands into `JettraStoreShell` from bash scripts:

```bash
cat << 'EOF' | java -jar target/JettraStoreShell-1.0-SNAPSHOT.jar
connect localhost 8086
login admin admin
record insert audit_logs tx_1 com.enterprise.model.AuditRecord {"txId":"tx_1","action":"LOGIN","userId":"admin"}
record get audit_logs tx_1
backup
exit
EOF
```
