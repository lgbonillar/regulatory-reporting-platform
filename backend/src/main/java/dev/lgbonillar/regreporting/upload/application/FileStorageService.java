package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.dto.StoredFile;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;

@Service
public class FileStorageService {

    private final Path uploadRoot;
    private final long maxFileSizeBytes;

    public FileStorageService(
            @Value("${app.storage.upload-dir}") String uploadDir,
            @Value("${app.storage.max-file-size-bytes}") long maxFileSizeBytes
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public StoredFile store(MultipartFile file, String username) {
        validate(file);

        try {
            String safeUsername = sanitizePathPart(username);
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String storedFilename = sanitizeFilename(originalFilename);

            Path userDirectory = uploadRoot.resolve(safeUsername).normalize();
            Files.createDirectories(userDirectory);

            Path targetPath = userDirectory.resolve(storedFilename).normalize();

            if (!targetPath.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Invalid storage path");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(
                    storedFilename,
                    uploadRoot.relativize(targetPath).toString().replace("\\", "/"),
                    sha256(targetPath)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store uploaded file", exception);
        }
    }

    public void delete(String relativeStoragePath) {
        Path filePath = uploadRoot
                .resolve(relativeStoragePath)
                .normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete uploaded file", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("File exceeds max allowed size");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are allowed");
        }
    }

    private String sanitizePathPart(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-+", "-");
    }

    private String sanitizeFilename(String filename) {
        return filename.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-+", "-");
    }

    private String sha256(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(inputStream.readAllBytes());

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    public Resource loadAsResource(String relativeStoragePath) {
        Path filePath = uploadRoot
                .resolve(relativeStoragePath)
                .normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new ResourceNotFoundException("Invalid storage path");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ResourceNotFoundException("Uploaded file not found");
        }

        return new FileSystemResource(filePath);
    }

}
