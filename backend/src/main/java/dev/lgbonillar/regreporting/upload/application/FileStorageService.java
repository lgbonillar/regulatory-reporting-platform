package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.dto.StoredFile;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.domain.User;

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
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRoot;
    private final long maxFileSizeBytes;
    private final UploadedFileRepository uploadedFileRepository;

    public FileStorageService(
            @Value("${app.storage.upload-dir}") String uploadDir,
            @Value("${app.storage.max-file-size-bytes}") long maxFileSizeBytes,
            UploadedFileRepository uploadedFileRepository
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.uploadedFileRepository = uploadedFileRepository;
    }

    public StoredFile store(MultipartFile file, User currentUser) {
        validate(file);

        String requestedFilename = Objects.requireNonNull(file.getOriginalFilename());
        String resolvedFilename = resolveUniqueOriginalFilename(currentUser.getId(), requestedFilename);

        String safeUsername = sanitizePathPart(currentUser.getUsername());
        String storedFilename = sanitizeFilename(resolvedFilename);

        Path userDirectory = uploadRoot.resolve(safeUsername).normalize();

        try {
            Files.createDirectories(userDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create user directory", exception);
        }

        Path targetPath = userDirectory.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(
                    resolvedFilename,
                    storedFilename,
                    uploadRoot.relativize(targetPath).toString().replace("\\", "/"),
                    sha256(targetPath)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store uploaded file", exception);
        }
    }

    public StoredFile replace(MultipartFile file, User currentUser, UploadedFile existingFile) {
        validate(file);

        String requestedFilename = Objects.requireNonNull(file.getOriginalFilename());
        String resolvedFilename = resolveUniqueOriginalFilenameExcluding(
                currentUser.getId(),
                requestedFilename,
                existingFile.getId()
        );

        String safeUsername = sanitizePathPart(currentUser.getUsername());
        String storedFilename = sanitizeFilename(resolvedFilename);

        Path userDirectory = uploadRoot.resolve(safeUsername).normalize();

        try {
            Files.createDirectories(userDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create user directory", exception);
        }

        Path targetPath = userDirectory.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        String previousStoragePath = existingFile.getStoragePath();
        boolean storagePathChanged = !uploadRoot.resolve(previousStoragePath).normalize().equals(targetPath);

        if (storagePathChanged) {
            deleteQuietly(previousStoragePath);
        }

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(
                    resolvedFilename,
                    storedFilename,
                    uploadRoot.relativize(targetPath).toString().replace("\\", "/"),
                    sha256(targetPath)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store replaced file", exception);
        }
    }

    public void delete(String relativeStoragePath) {
        Path filePath = uploadRoot.resolve(relativeStoragePath).normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete uploaded file", exception);
        }
    }

    private void deleteQuietly(String relativeStoragePath) {
        try {
            delete(relativeStoragePath);
        } catch (Exception ignored) {
        }
    }

    private String resolveUniqueOriginalFilename(UUID uploadedById, String requested) {
        String base;
        String extension;

        int dotIndex = requested.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < requested.length() - 1) {
            base = requested.substring(0, dotIndex);
            extension = requested.substring(dotIndex);
        } else {
            base = requested;
            extension = "";
        }

        String candidate = requested;
        int counter = 2;

        while (uploadedFileRepository.existsByUploadedByIdAndOriginalFilename(uploadedById, candidate)) {
            candidate = base + "(" + counter + ")" + extension;
            counter++;
        }

        return candidate;
    }

    private String resolveUniqueOriginalFilenameExcluding(UUID uploadedById, String requested, UUID excludedFileId) {
        String base;
        String extension;

        int dotIndex = requested.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < requested.length() - 1) {
            base = requested.substring(0, dotIndex);
            extension = requested.substring(dotIndex);
        } else {
            base = requested;
            extension = "";
        }

        String candidate = requested;
        int counter = 2;

        while (uploadedFileRepository.existsByUploadedByIdAndOriginalFilenameAndIdNot(uploadedById, candidate, excludedFileId)) {
            candidate = base + "(" + counter + ")" + extension;
            counter++;
        }

        return candidate;
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
        Path filePath = uploadRoot.resolve(relativeStoragePath).normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new ResourceNotFoundException("Invalid storage path");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ResourceNotFoundException("Uploaded file not found");
        }

        return new FileSystemResource(filePath);
    }

    public Path resolvePath(String relativeStoragePath) {
        Path filePath = uploadRoot.resolve(relativeStoragePath).normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new ResourceNotFoundException("Invalid storage path");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ResourceNotFoundException("Uploaded file not found");
        }

        return filePath;
    }

}