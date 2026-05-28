package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.Role;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import dev.lgbonillar.regreporting.users.infrastructure.UserRepository;
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

    @Mock
    private CurrentUserProvider currentUserProvider;

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
                visibleStatuses()
        )).thenReturn(List.of(file));
        when(uploadedFileMapper.toUploadedFileResponse(file)).thenReturn(response);

        List<UploadedFileResponse> result =
                uploadedFileQueryService.listUploadedFiles("analyst01");

        assertThat(result).containsExactly(response);

        verify(uploadedFileRepository).findByUploadedByIdAndStatusInOrderByUploadedAtDesc(
                user.getId(),
                visibleStatuses()
        );
    }

    @Test
    void getViewableUploadedFileReturnsFileForOwnerAnalyst() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();
        UploadedFile file = uploadedFile(UploadedFileStatus.PENDING_CORRECTION, analyst);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);

        UploadedFile result = uploadedFileQueryService.getViewableUploadedFile(fileId);

        assertThat(result).isSameAs(file);
    }

    @Test
    void getViewableUploadedFileReturnsFileForAdministrator() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = uploadedFile(UploadedFileStatus.PENDING_CORRECTION);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(currentUserProvider.getCurrentUser()).thenReturn(user(UserRole.ADMINISTRATOR));

        UploadedFile result = uploadedFileQueryService.getViewableUploadedFile(fileId);

        assertThat(result).isSameAs(file);
    }

    @Test
    void getViewableUploadedFileReturnsFileForRoot() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = uploadedFile(UploadedFileStatus.PENDING_CORRECTION);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(currentUserProvider.getCurrentUser()).thenReturn(user(UserRole.ROOT));

        UploadedFile result = uploadedFileQueryService.getViewableUploadedFile(fileId);

        assertThat(result).isSameAs(file);
    }

    @Test
    void getViewableUploadedFileThrowsForbiddenForAuditor() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = uploadedFile(UploadedFileStatus.PENDING_CORRECTION);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(currentUserProvider.getCurrentUser()).thenReturn(user(UserRole.AUDITOR));

        assertThatThrownBy(() -> uploadedFileQueryService.getViewableUploadedFile(fileId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You are not allowed to view this uploaded file");
    }

    @Test
    void getViewableUploadedFileThrowsForbiddenForAnotherAnalystFile() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = uploadedFile(UploadedFileStatus.PENDING_CORRECTION);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(currentUserProvider.getCurrentUser()).thenReturn(user(UserRole.ANALYST, "analyst02"));

        assertThatThrownBy(() -> uploadedFileQueryService.getViewableUploadedFile(fileId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You are not allowed to view this uploaded file");
    }

    private UploadedFile uploadedFile(UploadedFileStatus status) {
        return uploadedFile(status, analyst());
    }

    private UploadedFile uploadedFile(UploadedFileStatus status, User uploadedBy) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                status,
                uploadedBy
        );
    }

    private List<UploadedFileStatus> visibleStatuses() {
        return List.of(
                UploadedFileStatus.STORED,
                UploadedFileStatus.PENDING_CORRECTION,
                UploadedFileStatus.MISSING,
                UploadedFileStatus.FAILED
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
        return user(UserRole.ANALYST, "analyst01");
    }

    private User user(UserRole role) {
        return user(role, role.name().toLowerCase());
    }

    private User user(UserRole role, String username) {
        User user = new User(
                username,
                username + "@example.com",
                username,
                null,
                false,
                UserStatus.ACTIVE
        );
        user.setId(UUID.randomUUID());
        user.getRoles().add(new Role(role.name(), role.name(), null));
        return user;
    }

}
