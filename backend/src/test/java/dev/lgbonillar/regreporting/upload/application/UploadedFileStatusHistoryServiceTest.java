package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatusHistory;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileTransitionSource;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileStatusHistoryRepository;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedFileStatusHistoryServiceTest {

    @Mock
    private UploadedFileStatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private UploadedFileStatusHistoryService statusHistoryService;

    @Test
    void recordTransitionPersistsHistory() {
        UploadedFile uploadedFile = uploadedFile();
        User analyst = analyst();

        when(statusHistoryRepository.save(org.mockito.ArgumentMatchers.any(UploadedFileStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UploadedFileStatusHistory result = statusHistoryService.recordTransition(
                uploadedFile,
                UploadedFileStatus.PENDING_CORRECTION,
                UploadedFileStatus.STORED,
                UploadedFileTransitionSource.USER,
                analyst,
                "File validation passed"
        );

        ArgumentCaptor<UploadedFileStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(UploadedFileStatusHistory.class);

        verify(statusHistoryRepository).save(historyCaptor.capture());

        UploadedFileStatusHistory savedHistory = historyCaptor.getValue();

        assertThat(result).isSameAs(savedHistory);
        assertThat(savedHistory.getUploadedFile()).isSameAs(uploadedFile);
        assertThat(savedHistory.getPreviousStatus()).isEqualTo(UploadedFileStatus.PENDING_CORRECTION);
        assertThat(savedHistory.getNewStatus()).isEqualTo(UploadedFileStatus.STORED);
        assertThat(savedHistory.getTransitionSource()).isEqualTo(UploadedFileTransitionSource.USER);
        assertThat(savedHistory.getTransitionedBy()).isSameAs(analyst);
        assertThat(savedHistory.getReason()).isEqualTo("File validation passed");
        assertThat(savedHistory.getCreatedAt()).isNotNull();
    }

    @Test
    void listHistoryReturnsRepositoryResults() {
        UUID uploadedFileId = UUID.randomUUID();
        List<UploadedFileStatusHistory> history = List.of(
                new UploadedFileStatusHistory(
                        uploadedFile(),
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileTransitionSource.SYSTEM,
                        null,
                        "Storage resource not found"
                )
        );

        when(statusHistoryRepository.findAllByUploadedFile_IdOrderByCreatedAtAsc(uploadedFileId))
                .thenReturn(history);

        List<UploadedFileStatusHistory> result = statusHistoryService.listHistory(uploadedFileId);

        assertThat(result).isSameAs(history);
    }

    private UploadedFile uploadedFile() {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                analyst()
        );
    }

    private User analyst() {
        return new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }
}
