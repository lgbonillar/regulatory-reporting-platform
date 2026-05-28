package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.domain.User;
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

    public UploadedFileQueryService(
            UploadedFileRepository uploadedFileRepository,
            UserRepository userRepository,
            UploadedFileMapper uploadedFileMapper
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.userRepository = userRepository;
        this.uploadedFileMapper = uploadedFileMapper;
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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return uploadedFileRepository
                .findByUploadedByIdAndStatusInOrderByUploadedAtDesc(
                        user.getId(),
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
}
