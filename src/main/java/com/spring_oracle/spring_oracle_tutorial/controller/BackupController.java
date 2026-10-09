package com.spring_oracle.spring_oracle_tutorial.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/backups")
public class BackupController {

    @Value("${oracle.backup.local-folder:./backups}")
    private String localFolder;

    private Path getBackupDirectory() {
        return Paths.get(localFolder).toAbsolutePath().normalize();
    }

    // GET /api/backups
    // List all .dmp backup files
    @GetMapping
    public ResponseEntity<?> listBackups() throws IOException {
        Path directory = getBackupDirectory();

        if (!Files.isDirectory(directory)) {
            return ResponseEntity.ok(List.of());
        }

        try (Stream<Path> files = Files.list(directory)) {
            List<Map<String, Object>> backups = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .matches("oracle_backup_.*\\.dmp"))
                    .sorted(Comparator.comparing(
                            (Path path) -> path.getFileName().toString()).reversed())
                    .map(path -> {
                        try {
                            return Map.<String, Object>of(
                                    "filename", path.getFileName().toString(),
                                    "sizeBytes", Files.size(path),
                                    "downloadUrl", "/api/backups/"
                                            + path.getFileName());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            return ResponseEntity.ok(backups);
        }
    }

    // GET /api/backups/latest
    // Download the newest .dmp backup
    @GetMapping("/latest")
    public ResponseEntity<Resource> downloadLatestBackup()
            throws IOException {

        Path directory = getBackupDirectory();

        if (!Files.isDirectory(directory)) {
            return ResponseEntity.notFound().build();
        }

        Path latest;

        try (Stream<Path> files = Files.list(directory)) {
            latest = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .matches("oracle_backup_.*\\.dmp"))
                    .max(Comparator.comparing(
                            path -> path.getFileName().toString()))
                    .orElse(null);
        }

        if (latest == null) {
            return ResponseEntity.notFound().build();
        }

        return downloadFile(latest);
    }

    // GET /api/backups/{filename}
    // Download a specific .dmp backup
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadBackup(
            @PathVariable String filename) throws IOException {

        if (!filename.matches("oracle_backup_.*\\.dmp")) {
            return ResponseEntity.badRequest().build();
        }

        Path directory = getBackupDirectory();
        Path file = directory.resolve(filename).normalize();

        if (!file.getParent().equals(directory)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        return downloadFile(file);
    }

    private ResponseEntity<Resource> downloadFile(Path file)
            throws IOException {

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(file))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                file.getFileName() + "\"")
                .body(resource);
    }
}
