package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.UploadedFileResponse;
import com.mrcrafterman.regreporting.upload.infrastructure.UploadedFileRepository;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import com.mrcrafterman.regreporting.users.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedFileQueryServiceTest {

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UploadedFileMapper uploadedFileMapper;

    @InjectMocks
    private UploadedFileQueryService uploadedFileQueryService;

    @Test
    void getStoredUploadedFileReturnsStoredFile() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = uploadedFile(UploadedFileStatus.STORED);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));

        UploadedFile result = uploadedFileQueryService.getStoredUploadedFile(fileId);

        assertThat(result).isSameAs(file);
    }

    @Test
    void getStoredUploadedFileThrowsResourceNotFoundWhenFileDoesNotExist() {
        UUID fileId = UUID.randomUUID();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileQueryService.getStoredUploadedFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file not found");
    }

    @Test
    void getStoredUploadedFileThrowsResourceNotFoundWhenFileIsNotStored() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = uploadedFile(UploadedFileStatus.MISSING);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));

        assertThatThrownBy(() -> uploadedFileQueryService.getStoredUploadedFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file not found");
    }

    @Test
    void listUploadedFilesThrowsResourceNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileQueryService.listUploadedFiles("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void listUploadedFilesReturnsMappedFilesForUser() {
        User user = analyst();
        UploadedFile file = uploadedFile(UploadedFileStatus.STORED);
        UploadedFileResponse response = uploadedFileResponse(file);

        when(userRepository.findByUsername("analyst01")).thenReturn(Optional.of(user));
        when(uploadedFileRepository.findByUploadedByIdAndStatusInOrderByUploadedAtDesc(
                user.getId(),
                List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(List.of(file));
        when(uploadedFileMapper.toUploadedFileResponse(file)).thenReturn(response);

        List<UploadedFileResponse> result =
                uploadedFileQueryService.listUploadedFiles("analyst01");

        assertThat(result).containsExactly(response);

        verify(uploadedFileRepository).findByUploadedByIdAndStatusInOrderByUploadedAtDesc(
                user.getId(),
                List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        );
    }

    private UploadedFile uploadedFile(UploadedFileStatus status) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                status,
                analyst()
        );
    }

    private UploadedFileResponse uploadedFileResponse(UploadedFile file) {
        return new UploadedFileResponse(
                file.getId(),
                file.getOriginalFilename(),
                file.getStoredFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getChecksum(),
                file.getStatus().name(),
                file.getUploadedBy().getUsername(),
                file.getUploadedAt(),
                file.getUpdatedAt()
        );
    }

    private User analyst() {
        User user = new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
        user.setId(UUID.randomUUID());
        return user;
    }

}
