# JettraStoreShell - The Definitive Guide & Operational Manual

**JettraStoreShell** is the native, high-performance interactive Command-Line Interface (REPL) and administrative shell for **JettraStoreEngine**, engineered in Java 25. It enables database administrators, site reliability engineers, and software architects to interact with all 9 multi-model database engines, manage flexible ObjectId/DocumentId generation modes, inspect version histories with diffs, execute point-in-time restorations, and store immutable Java 25 Records directly from the terminal.

---

# Table of Contents
- [Chapter 1: Architecture & REPL Lifecycle](#chapter-1-architecture--repl-lifecycle)
  - [1.1 Non-blocking Connection Architecture](#11-non-blocking-connection-architecture)
  - [1.2 Command Dispatcher & Tokenizer](#12-command-dispatcher--tokenizer)
- [Chapter 2: Installation, Build & Configuration](#chapter-2-installation-build--configuration)
  - [2.1 Building from Source](#21-building-from-source)
  - [2.2 Launching and Command Flags](#22-launching-and-command-flags)
- [Chapter 3: Authentication & Session Management](#chapter-3-authentication--session-management)
  - [3.1 Node Connection (`connect`)](#31-node-connection-connect)
  - [3.2 JWT Authentication (`login`)](#32-jwt-authentication-login)
- [Chapter 4: ObjectId / DocumentId Generation Modes](#chapter-4-objectid--documentid-generation-modes)
  - [4.1 Manual Mode](#41-manual-mode)
  - [4.2 Auto-increment Sequence Mode (`auto`)](#42-auto-increment-sequence-mode-auto)
  - [4.3 Composite UUID Mode (`uuid`)](#43-composite-uuid-mode-uuid)
- [Chapter 5: Multi-Model Operations: CRUD & Edition](#chapter-5-multi-model-operations-crud--edition)
  - [5.1 `insert` - Multi-Model Ingestion](#51-insert---multi-model-ingestion)
  - [5.2 `edit` - Record & Document Updating](#52-edit---record--document-updating)
  - [5.3 `get` - Key/Object Lookup](#53-get---keyobject-lookup)
  - [5.4 `delete` / `rm` - Deletion](#54-delete--rm---deletion)
- [Chapter 6: Version History, Diffs & Point-in-Time Restoration](#chapter-6-version-history-diffs--point-in-time-restoration)
  - [6.1 `history` - Inspecting Historical Versions](#61-history---inspecting-historical-versions)
  - [6.2 `restore` - Point-in-Time Rollback](#62-restore---point-in-time-rollback)
- [Chapter 7: Dedicated Records Engine Command Suite (Java 25 Records)](#chapter-7-dedicated-records-engine-command-suite-java-25-records)
  - [7.1 `record insert`](#71-record-insert)
  - [7.2 `record edit`](#72-record-edit)
  - [7.3 `record get`](#73-record-get)
  - [7.4 `record history`](#74-record-history)
  - [7.5 `record restore`](#75-record-restore)
  - [7.6 `record delete`](#76-record-delete)
- [Chapter 8: Cluster Administration & Maintenance](#chapter-8-cluster-administration--maintenance)
  - [8.1 `status` - Node Health & Telemetry](#81-status---node-health--telemetry)
  - [8.2 `backup` - Snapshot Backups](#82-backup---snapshot-backups)
  - [8.3 `engines` - Multi-Model Catalog](#83-engines---multi-model-catalog)
  - [8.4 `users` & `rules`](#84-users--rules)
- [Chapter 9: Complete Command Reference Table](#chapter-9-complete-command-reference-table)

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
       │  - Command Parser & Tokenizer                │
       │  - JWT Bearer Session Cache                  │
       │  - IdMode Resolution Engine                  │
       │  - JettraStoreDriverJava Core Engine         │
       └──────────────────────────────────────────────┘
                               │
               HTTP / REST API (Port 8086)
                               │
                               ▼
       ┌──────────────────────────────────────────────┐
       │             JettraStoreEngine Node           │
       │  - 9 Multi-Model Database Engines            │
       │  - LSM-Tree / B-Tree Hybrid Storage Core     │
       │  - Composite IdGenerator & Version Control   │
       │  - Raft Quorum Distributed Consensus         │
       └──────────────────────────────────────────────┘
```

### 1.1 Non-blocking Connection Architecture
`JettraStoreShell` communicates with `JettraStoreEngine` over HTTP REST (default port `8086`) and gRPC. It leverages Java 25 Virtual Threads for immediate, non-blocking execution of queries, document writes, version scanning, and snapshot backups.

### 1.2 Command Dispatcher & Tokenizer
The REPL loop tokenizes input streams with support for:
- Command token matching (`insert`, `edit`, `get`, `delete`, `history`, `restore`, `record`, `engines`, `status`, `backup`, `help`, `exit`).
- Auto ID resolution (`auto`, `uuid`, or custom ID).
- Nested JSON payloads with escape preservation.

---

# Chapter 2: Installation, Build & Configuration

### 2.1 Building from Source
`JettraStoreShell` uses Apache Maven:

```bash
cd JettraStoreShell
mvn clean package -DskipTests
```

The build generates an executable shaded JAR in `target/JettraStoreShell-1.0-SNAPSHOT.jar`.

### 2.2 Launching and Command Flags
```bash
java --enable-preview -jar target/JettraStoreShell-1.0-SNAPSHOT.jar
```

---

# Chapter 3: Authentication & Session Management

### 3.1 Node Connection (`connect`)
```text
jettra> connect localhost 8086
Connecting to JettraStoreEngine at localhost:8086...
Connected successfully.
```

### 3.2 JWT Authentication (`login`)
```text
jettra> login admin admin
Login successful.
```

The shell stores the JWT bearer token in memory and automatically attaches it to all subsequent requests.

---

# Chapter 4: ObjectId / DocumentId Generation Modes

`JettraStoreShell` supports 3 ID generation strategies when inserting objects:

### 4.1 Manual Mode
Pass a custom string as the third parameter:
```text
jettra> insert document customers cust_1001 {"name":"Carlos Mendez","tier":"GOLD"}
Object inserted into DOCUMENT [customers:cust_1001].
```

### 4.2 Auto-increment Sequence Mode (`auto`)
Pass `auto` as the ID parameter to let the database generate an atomic sequence (`1, 2, 3...`):
```text
jettra> insert document invoices auto {"customer":"Acme Corp","total":1540.00}
Object inserted into DOCUMENT [invoices] with Auto-increment ID: 1

jettra> insert document invoices auto {"customer":"Beta LLC","total":3200.50}
Object inserted into DOCUMENT [invoices] with Auto-increment ID: 2
```

### 4.3 Composite UUID Mode (`uuid`)
Pass `uuid` as the ID parameter to generate a globally unique composite identifier:
```text
jettra> insert document events uuid {"event":"app_start","status":"OK"}
Object inserted into DOCUMENT [events] with Composite UUID: 8a7f1c2d-18dc93a4-a1b2-9f82ab34
```
*Composite UUID Format*: `[CPU/Host Digest]-[Timestamp Hex]-[Namespace Hash]-[Crypto UUID Suffix]`

---

# Chapter 5: Multi-Model Operations: CRUD & Edition

### 5.1 `insert` - Multi-Model Ingestion
**Syntax:**
```text
insert <model> <collection> <id|auto|uuid> <json_document>
```

**Examples across Multi-Model Engines:**
```text
# 1. Document (NoSQL JSON)
jettra> insert document products prod_01 {"title":"Ergonomic Chair","price":299.99}

# 2. Vector (AI Embeddings)
jettra> insert vector catalog vec_01 {"vector":[0.12, 0.85, 0.45],"label":"office_furniture"}

# 3. KeyValue (Memory Cache)
jettra> insert keyvalue session_cache token_xyz "user_admin_session_active"

# 4. Graph (LPG Nodes)
jettra> insert graph social node_carlos {"name":"Carlos","role":"Architect"}

# 5. TimeSeries (IoT Telemetry)
jettra> insert timeseries sensors 1755735000000 {"temperature":22.4,"humidity":65.0}

# 6. Column (OLAP Rows)
jettra> insert column analytics row_q3 {"revenue":150000,"growth":12.5}

# 7. Geospatial (2D GIS Points)
jettra> insert geospatial branches branch_pty {"lat":8.9824,"lon":-79.5199,"city":"Panama"}

# 8. Object (Binary BLOBs)
jettra> insert object media doc_pdf {"mime":"application/pdf","sizeBytes":1048576}
```

### 5.2 `edit` - Record & Document Updating
Updates an existing document while preserving previous versions in the historical LSM-Tree:
```text
jettra> edit document products prod_01 {"title":"Ergonomic Chair Pro","price":349.99,"inStock":true}
Object 'prod_01' in DOCUMENT [products] updated successfully (new version created).
```

### 5.3 `get` - Key/Object Lookup
```text
jettra> get document products prod_01
{"title":"Ergonomic Chair Pro","price":349.99,"inStock":true}
```

### 5.4 `delete` / `rm` - Deletion
```text
jettra> delete document products prod_01
Object 'prod_01' deleted from DOCUMENT [products].
```

---

# Chapter 6: Version History, Diffs & Point-in-Time Restoration

`JettraStoreEngine` tracks every change with full version timestamps. `JettraStoreShell` allows developers to inspect history and roll back instantly.

### 6.1 `history` - Inspecting Historical Versions
**Syntax:**
```text
history <model> <collection> <id>
```

**Example:**
```text
jettra> history document products prod_01
Version History for [DOCUMENT:products:prod_01]:
[
  {
    "versionNumber": 2,
    "timestamp": 1755735820000,
    "formattedDate": "2026-08-21 11:15:20",
    "payload": "{\"title\":\"Ergonomic Chair Pro\",\"price\":349.99,\"inStock\":true}",
    "isCurrent": true
  },
  {
    "versionNumber": 1,
    "timestamp": 1755735700000,
    "formattedDate": "2026-08-21 11:13:20",
    "payload": "{\"title\":\"Ergonomic Chair\",\"price\":299.99}",
    "isCurrent": false
  }
]
```

### 6.2 `restore` - Point-in-Time Rollback
**Syntax:**
```text
restore <model> <collection> <id> <timestamp>
```

**Example:**
```text
jettra> restore document products prod_01 1755735700000
Object 'prod_01' in DOCUMENT [products] restored to timestamp: 1755735700000

jettra> get document products prod_01
{"title":"Ergonomic Chair","price":299.99}
```

---

# Chapter 7: Dedicated Records Engine Command Suite (Java 25 Records)

The **`RECORDS`** engine provides typed storage for immutable Java 25 Records with automatic schema extraction.

### 7.1 `record insert`
```text
jettra> record insert employees emp_101 com.enterprise.model.EmployeeRecord {"id":"emp_101","fullName":"Carlos Mendez","department":"Engineering","salary":95000.0}
Java Record persisted in RECORDS engine [employees:emp_101] (com.enterprise.model.EmployeeRecord).
```

### 7.2 `record edit`
```text
jettra> record edit employees emp_101 com.enterprise.model.EmployeeRecord {"id":"emp_101","fullName":"Carlos Mendez","department":"Engineering","salary":105000.0}
Java Record persisted in RECORDS engine [employees:emp_101] (com.enterprise.model.EmployeeRecord).
```

### 7.3 `record get`
```text
jettra> record get employees emp_101
{
  "_recordClass": "com.enterprise.model.EmployeeRecord",
  "components": {
    "id": "emp_101",
    "fullName": "Carlos Mendez",
    "department": "Engineering",
    "salary": 105000.0
  }
}
```

### 7.4 `record history`
```text
jettra> record history employees emp_101
Record Version History [employees:emp_101]:
[
  { "versionNumber": 2, "timestamp": 1755736200000, "isCurrent": true, "payload": "..." },
  { "versionNumber": 1, "timestamp": 1755736100000, "isCurrent": false, "payload": "..." }
]
```

### 7.5 `record restore`
```text
jettra> record restore employees emp_101 1755736100000
Record restored to version from timestamp: 1755736100000
```

### 7.6 `record delete`
```text
jettra> record delete employees emp_101
Record 'emp_101' deleted successfully from [employees].
```

---

# Chapter 8: Cluster Administration & Maintenance

### 8.1 `status` - Node Health & Telemetry
```text
jettra> status
{
  "ram_usage": "256 MB / 4096 MB",
  "disk_usage": "1.2 GB / 500 GB",
  "nodes": "1 (Master)",
  "network": "ONLINE"
}
```

### 8.2 `backup` - Snapshot Backups
```text
jettra> backup
Backup snapshot triggered successfully.
```

### 8.3 `engines` - Multi-Model Catalog
```text
jettra> engines
Supported Multi-Model Engines (All 9 Engines):
  1. DOCUMENT   (Hierarchical JSON / NoSQL Documents with ID Strategies & History)
  2. RECORDS    (Immutable Java 25 Records with Schema Validation & Diffs)
  3. KEYVALUE   (High-Speed Cache & MemTable String Store)
  4. VECTOR     (AI ANN Cosine Embeddings & Vector Index)
  5. GRAPH      (LPG Nodes, Edges & Graph Traversal)
  6. TIMESERIES (IoT Sensor Telemetry & Metric WAL)
  7. COLUMN     (OLAP Columnar Vectors & Run-Length Rows)
  8. GEOSPATIAL (2D GIS Spatial Points & Distance Calculations)
  9. OBJECT     (Binary BLOBs, Chunked Block Store & Media)
```

---

# Chapter 9: Complete Command Reference Table

| Command | Arguments | Description |
|---|---|---|
| `connect` | `<host> <port>` | Connect to JettraStoreEngine instance |
| `login` | `<username> <password>` | Authenticate and obtain JWT token |
| `engines` | *(none)* | Display all 9 multi-model database engines |
| `insert` | `<model> <coll> <id\|auto\|uuid> <json>` | Insert object with Manual, Auto-increment, or UUID mode |
| `edit` | `<model> <coll> <id> <new_json>` | Update document/object and create new version |
| `get` | `<model> <coll> <id>` | Retrieve document/object by ID |
| `delete` / `rm` | `<model> <coll> <id>` | Delete document/object by ID |
| `history` | `<model> <coll> <id>` | Inspect full historical versions and diffs |
| `restore` | `<model> <coll> <id> <timestamp>` | Roll back document/object to historical timestamp |
| `record insert` | `<coll> <id\|auto\|uuid> <class> <json>` | Store Java 25 Record entity |
| `record edit` | `<coll> <id> <class> <new_json>` | Update Java 25 Record entity |
| `record get` | `<coll> <id>` | Retrieve Java 25 Record entity |
| `record history` | `<coll> <id>` | View Java 25 Record version history |
| `record restore` | `<coll> <id> <timestamp>` | Restore Java 25 Record to historical version |
| `record delete` | `<coll> <id>` | Delete Java 25 Record entity |
| `ref create` | `<engine> <db> <id> [node]` | Build ultra-fast cross-engine reference pointer |
| `ref resolve` | `<jref_uri>` | Direct O(1) resolve of cross-engine link |
| `backup` | *(none)* | Trigger manual snapshot backup |
| `status` | *(none)* | Display node RAM, Disk, and Quorum health |
| `users` | *(none)* | List configured users and roles |
| `rules` | *(none)* | Inspect active JettraRules constraints |
| `help` | *(none)* | Show command manual and examples |
| `exit` / `quit` | *(none)* | Close active shell session |
