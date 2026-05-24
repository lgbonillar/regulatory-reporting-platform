package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.UploadedFileResponse;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportFileServiceTest {

    @Mock
    private UploadedFileCommandService uploadedFileCommandService;

    @Mock
    private UploadedFileQueryService uploadedFileQueryService;

    @InjectMocks
    private ReportFileService reportFileService;

    @Test
    void uploadReportFileDelegatesToCommandService() {
        MockMultipartFile file = multipartFile();
        ReportFileUploadResponse expected = uploadResponse();

        when(uploadedFileCommandService.uploadReportFile(file)).thenReturn(expected);

        ReportFileUploadResponse result = reportFileService.uploadReportFile(file);

        assertThat(result).isSameAs(expected);
        verify(uploadedFileCommandService).uploadReportFile(file);
    }

    @Test
    void getStoredUploadedFileDelegatesToQueryService() {
        UUID fileId = UUID.randomUUID();
        UploadedFile expected = uploadedFile();

        when(uploadedFileQueryService.getStoredUploadedFile(fileId)).thenReturn(expected);

        UploadedFile result = reportFileService.getStoredUploadedFile(fileId);

        assertThat(result).isSameAs(expected);
        verify(uploadedFileQueryService).getStoredUploadedFile(fileId);
    }

    @Test
    void updateReportFileDelegatesToCommandService() {
        UUID fileId = UUID.randomUUID();
        MockMultipartFile file = multipartFile();
        ReportFileUploadResponse expected = uploadResponse();

        when(uploadedFileCommandService.updateReportFile(fileId, file)).thenReturn(expected);

        ReportFileUploadResponse result = reportFileService.updateReportFile(fileId, file);

        assertThat(result).isSameAs(expected);
        verify(uploadedFileCommandService).updateReportFile(fileId, file);
    }

    @Test
    void deleteUploadedFileDelegatesToCommandService() {
        UUID fileId = UUID.randomUUID();

        reportFileService.deleteUploadedFile(fileId);

        verify(uploadedFileCommandService).deleteUploadedFile(fileId);
    }

    @Test
    void listUploadedFilesDelegatesToQueryService() {
        List<UploadedFileResponse> expected = List.of();

        when(uploadedFileQueryService.listUploadedFiles("analyst01")).thenReturn(expected);

        List<UploadedFileResponse> result = reportFileService.listUploadedFiles("analyst01");

        assertThat(result).isSameAs(expected);
        verify(uploadedFileQueryService).listUploadedFiles("analyst01");
    }

    @Test
    void markUploadedFileAsMissingDelegatesToCommandService() {
        UUID fileId = UUID.randomUUID();

        reportFileService.markUploadedFileAsMissing(fileId);

        verify(uploadedFileCommandService).markUploadedFileAsMissing(fileId);
    }

    private MockMultipartFile multipartFile() {
        return new MockMultipartFile(
                "file",
                "report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "content".getBytes()
        );
    }

    private ReportFileUploadResponse uploadResponse() {
        return new ReportFileUploadResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "report.xlsx",
                "STORED",
                "PENDING_EXECUTION",
                "File uploaded"
        );
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
