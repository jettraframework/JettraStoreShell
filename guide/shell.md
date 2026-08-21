# Guía del Shell Interactivo (JettraDB Shell)

JettraStoreEngine incluye un poderoso **Shell Interactivo (REPL)** basado en consola. Con él puedes administrar el clúster, crear bases de datos, y ejecutar operaciones CRUD, transacciones y agregaciones en tiempo real de forma similar al shell de MongoDB pero abarcando los múltiples motores de Jettra.

---

## 1. Instalación y Conexión

Si has compilado el proyecto completo, el shell estará disponible en el módulo `JettraStoreShell`.

**Para ejecutar el shell:**
```bash
java -jar target/jettra-shell-1.0.0-SNAPSHOT.jar
```

**Para conectar a un nodo remoto:**
```bash
jettra> connect http://localhost:8080
jettra> login admin
Password: [admin]
```

---

## 2. Administración de Bases de Datos

El shell permite gestionar las bases de datos lógicas (que soportan de forma nativa los 9 motores de almacenamiento).

```bash
# Crear base de datos persistente
jettra> db create library_db

# Crear base de datos en memoria
jettra> db create cache_db --storage MEMORY

# Listar los 9 motores disponibles
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

# Listar bases de datos
jettra> show dbs

# Seleccionar la base de datos a usar
jettra> use library_db
```

---

## 3. Inserciones Multimodelo & Motor Records (Java 25)

Puedes insertar datos dinámicamente usando notación tipo JSON especificando el modelo subyacente. Si no se especifica, se asume `DOCUMENT`.

```bash
# Motor Documental (Por defecto)
jettra> insert document users usr_1 {"name": "Alice", "role": "admin"}

# Motor Vectorial (Embeddings AI)
jettra> insert vector embeddings vec_1 {"vector": [0.2, 0.5, 0.8], "label": "search_query"}

# Motor Clave-Valor
jettra> insert keyvalue sessions session_123 "token_xyz"

# Motor de Grafos
jettra> insert graph social node_alice {"name": "Alice", "age": 30}

# Motor Records (Java 25 Immutable Records)
jettra> record insert employees emp_101 com.enterprise.model.EmployeeRecord {"id": "emp_101", "name": "Carlos Mendez", "salary": 92000.0}

# Obtener Record
jettra> record get employees emp_101

# Eliminar Record
jettra> record delete employees emp_101
```

---

## 4. Consultas y Agregaciones

### Búsquedas básicas
```bash
# Buscar por ID
jettra> db.users.findById("usr_1")

# Buscar con filtros (Fluent API interno del shell)
jettra> db.users.find({ "role": "admin" })
```

### Agregaciones
El shell soporta pipelines de agregación inspirados en MongoDB, ejecutándose de forma distribuida en el clúster Jettra.

```bash
# Sumar ventas totales por categoría
jettra> db.sales.aggregate([
    { $match: { status: "COMPLETED" } },
    { $group: { _id: "$category", total: { $sum: "$amount" } } }
])

# Obtener métricas de un motor columnar
jettra> db.metrics_cf.column.aggregate([
    { $group: { _id: null, avgLoad: { $avg: "$cpu_load" } } }
])
```

---

## 5. Transacciones Interactivas (2PC)

El shell permite probar transacciones bloqueantes y operaciones atómicas de forma manual. Esto es útil para debuggear flujos de negocio.

```bash
# Iniciar la transacción y obtener un TX ID
jettra> tc begin
Transaction started. TxID: tx_89123

# Enviar operaciones atadas a la transacción (no se aplican hasta el commit)
jettra> db.accounts.update({ "_id": "acc01", "balance": 900 }, { tx: "tx_89123" })
jettra> db.accounts.update({ "_id": "acc02", "balance": 1100 }, { tx: "tx_89123" })

# Preparar la transacción en los grupos de Raft (Fase 1)
jettra> tc prepare tx_89123
Prepared successfully on 2/2 groups.

# Ejecutar el Commit (Fase 2)
jettra> tc commit tx_89123
Commit successful. Data is now visible.
```

**Ejemplo de Rollback:**
```bash
jettra> tc begin
Transaction started. TxID: tx_99999

jettra> db.inventory.update({ "_id": "item1", "stock": -5 }, { tx: "tx_99999" })
Error: Negatives not allowed by domain constraint.

# Rollback manual (Libera los bloqueos en el clúster)
jettra> tc abort tx_99999
Transaction tx_99999 aborted successfully.
```

---

## 6. Operaciones Avanzadas en Shell (Paginación, Agrupación, Estadísticas)

El shell permite realizar operaciones avanzadas sobre los datos empleando comandos de agregación y opciones extendidas de consulta.

### Paginación y Ordenación
```bash
# Consultar los últimos 10 logs de acceso (página 1, tamaño 10)
jettra> db.access_logs.find().sort({ "timestamp": -1 }).skip(0).limit(10)

# Ir a la página 2
jettra> db.access_logs.find().sort({ "timestamp": -1 }).skip(10).limit(10)
```

### Agrupación y Totales
```bash
# Calcular ventas totales agrupadas por región
jettra> db.sales.aggregate([
    { $group: { 
        _id: "$region", 
        totalSales: { $sum: "$amount" },
        count: { $sum: 1 }
    }},
    { $sort: { totalSales: -1 } }
])
```

### Cálculos Estadísticos
```bash
# Obtener métricas estadísticas (Max, Min, Avg) de uso de CPU en servidores
jettra> db.server_metrics.aggregate([
    { $group: { 
        _id: null, 
        avgCpu: { $avg: "$cpu_load" },
        maxCpu: { $max: "$cpu_load" },
        minCpu: { $min: "$cpu_load" }
    }}
])
```

---

## 7. Comandos de Administración del Clúster (WUI equivalentes)

Desde el shell interactivo también puedes acceder a las funcionalidades de administración similares a las de la interfaz web (`/wui`).

### Estado del Clúster
```bash
# Ver información de RAM, Disco y Nodos Conectados
jettra> status
{
  "ram_usage": "256 MB / 4096 MB",
  "disk_usage": "1.2 GB / 500 GB",
  "nodes": "1 (Master)",
  "network": "ONLINE"
}
```

### Usuarios y Roles
```bash
# Listar usuarios y sus privilegios
jettra> users
User list: [admin, guest]. Active nodes: 1 (Master).
```

### Reglas y Triggers (JettraStore)
```bash
# Visualizar el estado y ejecución de reglas
jettra> rules
Rules triggered: none. Triggers are active.
```

### Backup y Restauración
```bash
# Iniciar un respaldo completo manual
jettra> backup
Backup triggered successfully.
```
