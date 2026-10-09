# Oracle Database Backup — README

## 1. Project Information

**Project:** `spring_oracle_tutorial`

**Database:** Oracle AI Database Free

**Connection:** `localhost:1522/FREEPDB1`

**Backup tool:** Oracle Data Pump (`expdp`)

**Local backup directory:**

```text
backups/
```

## 2. Open the Project

```bash
cd "$HOME/Documents/Project Testing/Oracle/spring_oracle_tutorial"
```

## 3. Start Spring Boot

```bash
./mvnw spring-boot:run
```

Spring Boot runs the scheduled backup job while the application is running.

Press `Control + C` to stop the application.

## 4. Backup Configuration

File: `src/main/resources/application.properties`

```properties
# Oracle database
spring.datasource.url=jdbc:oracle:thin:@localhost:1522/FREEPDB1
spring.datasource.username=system
spring.datasource.password=${oracle.backup.password}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Oracle backup
oracle.backup.enabled=true
oracle.backup.username=system
oracle.backup.password=YOUR_ORACLE_PASSWORD
oracle.backup.connect=//localhost:1522/FREEPDB1
oracle.backup.schema=SYSTEM
oracle.backup.directory=DATA_PUMP_DIR
oracle.backup.directory-path=/opt/oracle/admin/FREE/dpdump/5621983B6DFB0909E0630B00580A528D

# Oracle Data Pump executable on macOS
oracle.backup.expdp-path=/Users/testing/oracle/instantclient_23_26/expdp

# Oracle container used to retrieve exported files
oracle.backup.docker-command=docker
oracle.backup.container=oracle-db-v2

# Local backup directory
oracle.backup.local-folder=./backups

# Testing: run every minute
oracle.backup.cron=0 * * * * *
```

Replace `YOUR_ORACLE_PASSWORD` with your actual Oracle password.

**Security:** Do not commit real database credentials to a public Git repository.

## 5. Backup Schedule

### Run every minute (testing)

```properties
oracle.backup.cron=0 * * * * *
```

### Run daily at 2:00 AM

```properties
oracle.backup.cron=0 0 2 * * *
```

The daily schedule uses the timezone configured for your Spring Boot application.

## 6. Check Backup Files

List all backup files:

```bash
ls -lh backups
```

Count the `.dmp` files:

```bash
find backups -maxdepth 1 -type f -name "*.dmp" | wc -l
```

Show the newest dump files:

```bash
ls -lt backups/*.dmp
```

Show the latest backup log files:

```bash
ls -lt backups/*.log
```

## 7. Maximum 5 Backups

The backup service uses:

```java
private static final int MAX_BACKUPS = 5;
```

After a new export and file copy succeed, the service keeps the newest five matching timestamped `.dmp` files and deletes older local dump files and their matching `.log` files.

Example:

```text
backups/
├── oracle_backup_2026-10-09_12-25-00.dmp
├── oracle_backup_2026-10-09_12-25-00.log
├── oracle_backup_2026-10-09_12-26-00.dmp
├── oracle_backup_2026-10-09_12-26-00.log
└── ...
```

Only five `.dmp` files are retained by the cleanup logic.

## 8. Oracle Data Pump Commands

Check the installed Data Pump version:

```bash
expdp HELP=YES
```

Connect to Oracle:

```bash
sqlplus system@//localhost:1522/FREEPDB1
```

Check the Data Pump directory:

```sql
SELECT directory_name, directory_path
FROM dba_directories
WHERE directory_name = 'DATA_PUMP_DIR';
```

Exit SQL*Plus:

```sql
EXIT;
```

## 9. Rebuild and Restart

Stop Spring Boot using `Control + C`.

Build the project:

```bash
./mvnw clean package
```

Run the application again:

```bash
./mvnw spring-boot:run
```

## 10. Troubleshooting

### `Cannot run program "expdp"`

Verify the executable:

```bash
which expdp
```

Expected path:

```text
/Users/testing/oracle/instantclient_23_26/expdp
```

Ensure `oracle.backup.expdp-path` matches that path.

### `UDE-00002: invalid username or password`

Check these properties:

```properties
oracle.backup.username=system
oracle.backup.password=YOUR_ORACLE_PASSWORD
oracle.backup.connect=//localhost:1522/FREEPDB1
```

Confirm that the credentials work with SQL*Plus.

### `ORA-12162: TNS:net service name is incorrectly specified`

Check the connection format:

```properties
oracle.backup.connect=//localhost:1522/FREEPDB1
```

### Backup file is missing locally

Check the application logs and confirm that the Oracle container name and `oracle.backup.directory-path` match the actual database configuration.

The current service uses Docker internally to copy the exported files from the container to the Mac.

## 11. Important Notes

* The Spring Boot application must be running for scheduled backups to execute.
* The service exports the schema configured by `oracle.backup.schema`.
* The `.dmp` file is a binary Data Pump export that can be used for database recovery or migration.
* Keep additional copies of important backups on separate storage.
* The retention cleanup removes old local backups permanently.
* The current service does not provide an immediate manual backup command; scheduled execution is controlled by `oracle.backup.cron`.
