
package com.spring_oracle.spring_oracle_tutorial.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class OracleBackupService {

        private static final int MAX_BACKUPS = 10;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        @Value("${oracle.backup.enabled:true}")
        private boolean enabled;

        @Value("${oracle.backup.username}")
        private String username;

        @Value("${oracle.backup.password}")
        private String password;

        @Value("${oracle.backup.connect}")
        private String connect;

        @Value("${oracle.backup.schema}")
        private String schema;

        @Value("${oracle.backup.directory}")
        private String directory;

        @Value("${oracle.backup.directory-path}")
        private String directoryPath;

        @Value("${oracle.backup.container}")
        private String containerName;

        @Value("${oracle.backup.local-folder:./backups}")
        private String localFolder;

        @Value("${oracle.backup.expdp-path}")
        private String expdpPath;

        @Value("${oracle.backup.docker-command:docker}")
        private String dockerCommand;

        @Scheduled(cron = "${oracle.backup.cron}")
        public void backupDatabase() {

                if (!enabled) {
                        return;
                }

                String effectivePassword = (password != null && !password.isBlank())
                                ? password
                                : System.getenv("ORACLE_DB_PASSWORD");

                if (effectivePassword == null || effectivePassword.isBlank()) {
                        System.err.println(
                                        "Oracle backup failed: Password is not configured.");
                        return;
                }

                String timestamp = LocalDateTime.now().format(FORMATTER);
                String dumpName = "oracle_backup_" + timestamp + ".dmp";
                String logName = "oracle_backup_" + timestamp + ".log";

                Path backupDirectory = Paths.get(localFolder)
                                .toAbsolutePath()
                                .normalize();

                try {
                        Files.createDirectories(backupDirectory);

                        System.out.println("Starting Oracle backup: " + dumpName);

                        List<String> exportCommand = List.of(
                                        expdpPath,
                                        username + "/" + effectivePassword + "@" + connect,
                                        "SCHEMAS=" + schema,
                                        "DIRECTORY=" + directory,
                                        "DUMPFILE=" + dumpName,
                                        "LOGFILE=" + logName,
                                        "REUSE_DUMPFILES=NO");

                        ProcessBuilder exportBuilder = new ProcessBuilder(exportCommand);

                        exportBuilder.redirectErrorStream(true);

                        Process exportProcess = exportBuilder.start();

                        String exportOutput = new String(
                                        exportProcess.getInputStream().readAllBytes(),
                                        StandardCharsets.UTF_8);

                        int exitCode = exportProcess.waitFor();

                        System.out.println(exportOutput);

                        if (exitCode != 0) {
                                throw new IOException(
                                                "Oracle Data Pump export failed. Exit code: "
                                                                + exitCode);
                        }

                        // Copy exported files from the Oracle container to macOS.
                        copyFromContainer(
                                        directoryPath + "/" + dumpName,
                                        backupDirectory.resolve(dumpName));

                        copyFromContainer(
                                        directoryPath + "/" + logName,
                                        backupDirectory.resolve(logName));

                        System.out.println(
                                        "Oracle backup completed: "
                                                        + backupDirectory.resolve(dumpName));

                        // Keep only the five newest successful local dump files.
                        cleanupOldBackups(backupDirectory);

                } catch (Exception e) {
                        System.err.println(
                                        "Oracle backup failed: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private void copyFromContainer(
                        String containerFile,
                        Path destination) throws IOException, InterruptedException {

                ProcessBuilder copyBuilder = new ProcessBuilder(
                                dockerCommand,
                                "cp",
                                containerName + ":" + containerFile,
                                destination.toString());

                copyBuilder.redirectErrorStream(true);

                Process process = copyBuilder.start();

                String output = new String(
                                process.getInputStream().readAllBytes(),
                                StandardCharsets.UTF_8);

                int exitCode = process.waitFor();

                if (exitCode != 0) {
                        throw new IOException(
                                        "Failed to copy " + containerFile + ": " + output);
                }

                if (!Files.exists(destination) || Files.size(destination) == 0) {
                        throw new IOException(
                                        "Backup file is missing or empty: " + destination);
                }
        }

        private void cleanupOldBackups(Path backupDirectory)
                        throws IOException {

                List<Path> dumpFiles;

                try (Stream<Path> files = Files.list(backupDirectory)) {
                        dumpFiles = files
                                        .filter(Files::isRegularFile)
                                        .filter(path -> path.getFileName()
                                                        .toString()
                                                        .matches(
                                                                        "oracle_backup_\\d{4}-\\d{2}-\\d{2}_"
                                                                                        + "\\d{2}-\\d{2}-\\d{2}\\.dmp"))
                                        .sorted(Comparator.comparing(
                                                        (Path path) -> path.getFileName().toString()).reversed())
                                        .toList();
                }

                for (int i = MAX_BACKUPS; i < dumpFiles.size(); i++) {
                        Path oldDump = dumpFiles.get(i);

                        String dumpFileName = oldDump.getFileName().toString();
                        String logFileName = dumpFileName.substring(
                                        0, dumpFileName.length() - ".dmp".length()) + ".log";

                        Files.deleteIfExists(oldDump);
                        Files.deleteIfExists(
                                        backupDirectory.resolve(logFileName));

                        System.out.println(
                                        "Deleted old backup: " + dumpFileName);
                }

                System.out.println(
                                "Backup retention completed. Current dump count: "
                                                + Math.min(dumpFiles.size(), MAX_BACKUPS));
        }
}