# MyDB – AI Powered Database Manager

## Overview

MyDB is a JavaFX desktop application that makes working with MySQL databases easier. It provides a graphical interface for viewing and editing tables, running SQL and AI‑assisted queries, visualizing data, creating backups, and monitoring risky queries. The goal is to reduce the barrier to learning DBMS concepts while still teaching good practices.

---

## Features

### 1. Table View (CRUD)
- Browse databases, tables, and columns in a sidebar tree.
- Open any table in a spreadsheet‑like view.
- Add, edit, and delete records using simple forms.
- See record counts and feedback messages for operations.

### 2. Query Editor (MySQL + AI)
- Two modes:
  - MySQL Mode: Write and execute raw SQL queries.
  - AI Mode: Type a question in English and let Google Gemini generate SQL.
- Results are shown in a `TableView`.
- Actions:
  - Execute query
  - Clear editor/results
  - Copy query text to clipboard
  - Export results to CSV or JSON

### 3. Data Visualization
- Generate charts (bar, line, pie, etc.) from tables or query results.
- Helps quickly understand trends, comparisons, and distributions.
- Useful for lab demos, reports, and presentations.

### 4. Backup & Recovery
- Export single tables or entire databases to formats such as SQL/CSV/JSON.
- Restore data from previous exports.
- Designed to be safe and simple for beginners.

### 5. Security Monitor
- Scans SQL text for obviously dangerous patterns (e.g., destructive commands, always‑true conditions like `OR 1=1`).[web:254][web:287]
- Warns the user before executing risky queries.
- Logs suspicious commands to help teach safe database habits.[web:292][web:296]

### 6. Help & About
- Built‑in help tab with:
  - How‑to instructions for each module.
  - Tips for writing SQL and using AI mode.
  - Information about the project and technologies used.

### 7. MySQL Documentation
- Built-in MySQL documentation tab using JavaFX WebView.
- Includes quick local tips for query analysis, indexing, transactions, and safe SQL habits.

---

## Technology Stack

- Language & UI:
  - Java (JDK, e.g., 21)
  - JavaFX (desktop GUI)[web:304]

- Database:
  - MySQL (RDBMS)[web:303]
  - JDBC with MySQL Connector/J (MySQL driver)[web:303]

- AI / NLP:
  - Google Gemini API (natural language to SQL)

- Networking & Data:
  - OkHttp (HTTP client for API calls)[web:305]
  - Gson (JSON parsing/serialization)

- Security & UX:
  - Ikonli FontAwesome icons (modern UI icons)[web:305]
  - JavaFX CSS styling (themes)

- Project Utilities (custom):
  - `DatabaseConnection` – central DB connection helper
  - `DatabaseSchemaExtractor` – builds schema text for AI
  - `SecurityMonitor` – analyzes SQL for risky patterns
  - `BackupService` / `BackupController` – backup & restore
  - `VisualizationController` – charts and graphs
  - `SessionManager` – manages current DB/session

---

## Setup

1. **Install prerequisites**
   - JDK (e.g., 21 or a compatible version).
   - MySQL server installed and running.

2. **Configure database**
   - Create your database in MySQL.
   - Note:
     - Host (e.g., `localhost`)
     - Port (default `3306`)
     - Database name
     - Username and password
   - Update MyDB’s configuration (e.g., `DatabaseConnection` class or config file) with these values.

3. **Configure Gemini API**
   - Get a Google Gemini API key.
   - In the app, open “AI Settings” and paste your API key to enable AI mode.

---

## How to Build and Run

### Running Locally with Maven

1. Clean and build the project:
   ```bash
   mvn clean compile
   ```

2. Run the JavaFX application:
   ```bash
   mvn javafx:run
   ```

### Packaging (Creating a Windows Installer/EXE)

To package the application into a standalone Windows installer using `jpackage`, follow these steps:

1. **Clean and Package** the application to generate the JAR file:
   ```bash
   mvn clean package
   ```

2. **Copy dependencies** into the packaging input folder:
   ```bash
   mvn dependency:copy-dependencies -DoutputDirectory=target/package-input
   ```

3. **Copy the main application JAR** into the packaging input folder:
   ```powershell
   Copy-Item target\MyDB-1.0.0.jar target\package-input\MyDB-1.0.0.jar
   ```

4. **Run `jpackage`** (make sure to specify `com.mydb.Launcher` as the main class to avoid JavaFX launcher issues on the classpath):
   ```powershell
   jpackage `
     --type exe `
     --name MyDB `
     --app-version 1.0.0 `
     --vendor "github.com/Vaibhav0248" `
     --input target\package-input `
     --main-jar MyDB-1.0.0.jar `
     --main-class com.mydb.Launcher `
     --icon src\main\resources\icons\logo.ico `
     --dest target\installer `
     --win-menu `
     --win-shortcut `
     --win-dir-chooser `
     --win-per-user-install `
     --java-options "--enable-native-access=ALL-UNNAMED"
   ```

---

## Contributions
This project represents the collective team efforts.
