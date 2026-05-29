package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import dev.lgbonillar.regreporting.users.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UploadedFileQueryService {

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;
    private final UploadedFileMapper uploadedFileMapper;
    private final CurrentUserProvider currentUserProvider;

    public UploadedFileQueryService(
            UploadedFileRepository uploadedFileRepository,
            UserRepository userRepository,
            UploadedFileMapper uploadedFileMapper,
            CurrentUserProvider currentUserProvider
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.userRepository = userRepository;
        this.uploadedFileMapper = uploadedFileMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public UploadedFile getStoredUploadedFile(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        if (uploadedFile.getStatus() != UploadedFileStatus.STORED) {
            throw new ResourceNotFoundException("Uploaded file not found");
        }

        return uploadedFile;
    }

    @Transactional(readOnly = true)
    public List<UploadedFileResponse> listUploadedFiles(String username) {
        User currentUser = currentUserProvider.getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!canListAs(currentUser, targetUser)) {
            throw new ForbiddenOperationException(
                    "You are not allowed to list files for this user"
            );
        }

        return uploadedFileRepository
                .findByUploadedByIdAndStatusInOrderByUploadedAtDesc(
                        targetUser.getId(),
                        List.of(
                                UploadedFileStatus.STORED,
                                UploadedFileStatus.PENDING_CORRECTION,
                                UploadedFileStatus.MISSING,
                                UploadedFileStatus.FAILED
                        )
                )
                .stream()
                .map(uploadedFileMapper::toUploadedFileResponse)
                .toList();
    }

    private boolean canListAs(User currentUser, User targetUser) {
        if (currentUser.hasRole(UserRole.ROOT) ||
                currentUser.hasRole(UserRole.ADMINISTRATOR)) {
            return true;
        }
        if (currentUser.hasRole(UserRole.ANALYST)) {
            return currentUser.getId().equals(targetUser.getId());
        }
        return false;
    }

    @Transactional(readOnly = true)
    public UploadedFile getViewableUploadedFile(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        User currentUser = currentUserProvider.getCurrentUser();
        boolean isPrivileged = currentUser.hasRole(UserRole.ROOT) ||
                               currentUser.hasRole(UserRole.ADMINISTRATOR);
        boolean isOwner = currentUser.hasRole(UserRole.ANALYST) &&
                uploadedFile.getUploadedBy()
                        .getUsername()
                        .equals(currentUser.getUsername());

        if (!isPrivileged && !isOwner) {
            throw new ForbiddenOperationException(
                    "You are not allowed to view this uploaded file"
            );
        }

        if (!isPrivileged && uploadedFile.getStatus() != UploadedFileStatus.STORED) {
            throw new ResourceNotFoundException("Uploaded file not found");
        }

        return uploadedFile;
    }

    private void requireCanView(UploadedFile uploadedFile) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (currentUser.hasRole(UserRole.ROOT) ||
                currentUser.hasRole(UserRole.ADMINISTRATOR)) {
            return;
        }

        if (currentUser.hasRole(UserRole.ANALYST) &&
                uploadedFile.getUploadedBy()
                        .getUsername()
                        .equals(currentUser.getUsername())) {
            return;
        }

        throw new ForbiddenOperationException(
                "You are not allowed to view this uploaded file"
        );
    }
}
