package dev.lgbonillar.regreporting.upload;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.upload.application.ReportFileService;
import dev.lgbonillar.regreporting.upload.controller.ReportFileController;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileFindingResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileValidationRunResponse;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportFileController.class)
class ReportFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportFileService reportFileService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void listReportFilesReturnsFiles() throws Exception {
        UploadedFileResponse response = uploadedFileResponse();

        when(reportFileService.listUploadedFiles("analyst01")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/report-files")
                        .param("username", "analyst01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.count").value(1))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].originalFilename").value("report.xlsx"))
                .andExpect(jsonPath("$.data[0].fileStatus").value("STORED"));

        verify(reportFileService).listUploadedFiles("analyst01");
    }

    @Test
    void uploadReportFileReturnsUploadResponse() throws Exception {
        MockMultipartFile file = multipartFile();
        ReportFileUploadResponse response = uploadResponse();

        when(reportFileService.uploadReportFile(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/report-files")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalFilename").value("report.xlsx"))
                .andExpect(jsonPath("$.data.fileStatus").value("STORED"))
                .andExpect(jsonPath("$.data.jobStatus").value("PENDING_EXECUTION"));

        verify(reportFileService).uploadReportFile(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateReportFileReturnsUploadResponse() throws Exception {
        UUID fileId = UUID.randomUUID();
        MockMultipartFile file = multipartFile();
        ReportFileUploadResponse response = uploadResponse();

        when(reportFileService.updateReportFile(
                org.mockito.ArgumentMatchers.eq(fileId),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(response);

        mockMvc.perform(multipart("/api/report-files/{fileId}", fileId)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalFilename").value("report.xlsx"))
                .andExpect(jsonPath("$.data.fileStatus").value("STORED"));

        verify(reportFileService).updateReportFile(
                org.mockito.ArgumentMatchers.eq(fileId),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void deleteReportFileReturnsNoContent() throws Exception {
        UUID fileId = UUID.randomUUID();

        mockMvc.perform(delete("/api/report-files/{fileId}", fileId))
                .andExpect(status().isNoContent());

        verify(reportFileService).deleteUploadedFile(fileId);
    }

    @Test
    void downloadReportFileReturnsResource() throws Exception {
        UUID fileId = UUID.randomUUID();
        UploadedFile uploadedFile = uploadedFile();
        ByteArrayResource resource = new ByteArrayResource("content".getBytes());

        when(reportFileService.getStoredUploadedFile(fileId)).thenReturn(uploadedFile);
        when(fileStorageService.loadAsResource(uploadedFile.getStoragePath())).thenReturn(resource);

        mockMvc.perform(get("/api/report-files/{fileId}/download", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("report.xlsx")));

        verify(reportFileService).getStoredUploadedFile(fileId);
        verify(fileStorageService).loadAsResource(uploadedFile.getStoragePath());
    }

    @Test
    void downloadReportFileMarksFileAsMissingWhenStorageResourceDoesNotExist() throws Exception {
        UUID fileId = UUID.randomUUID();
        UploadedFile uploadedFile = uploadedFile();

        when(reportFileService.getStoredUploadedFile(fileId)).thenReturn(uploadedFile);
        when(fileStorageService.loadAsResource(uploadedFile.getStoragePath()))
                .thenThrow(new ResourceNotFoundException("File not found"));

        mockMvc.perform(get("/api/report-files/{fileId}/download", fileId))
                .andExpect(status().isNotFound());

        verify(reportFileService).markUploadedFileAsMissing(fileId);
    }

    @Test
    void listValidationRunsReturnsRuns() throws Exception {
        UUID fileId = UUID.randomUUID();
        UploadedFileValidationRunResponse response = validationRunResponse(fileId);

        when(reportFileService.listValidationRuns(fileId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/report-files/{fileId}/validation-runs", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.count").value(1))
                .andExpect(jsonPath("$.data[0].status").value("FAILED"))
                .andExpect(jsonPath("$.data[0].source").value("UPLOAD"));

        verify(reportFileService).listValidationRuns(fileId);
    }

    @Test
    void listFindingsReturnsFindings() throws Exception {
        UUID fileId = UUID.randomUUID();
        UploadedFileFindingResponse response = findingResponse(fileId, UUID.randomUUID());

        when(reportFileService.listFindings(fileId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/report-files/{fileId}/findings", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.count").value(1))
                .andExpect(jsonPath("$.data[0].severity").value("ERROR"))
                .andExpect(jsonPath("$.data[0].code").value("INVALID_STRUCTURE"));

        verify(reportFileService).listFindings(fileId);
    }

    @Test
    void listValidationRunFindingsReturnsFindings() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID validationRunId = UUID.randomUUID();
        UploadedFileFindingResponse response = findingResponse(fileId, validationRunId);

        when(reportFileService.listFindingsByValidationRun(fileId, validationRunId))
                .thenReturn(List.of(response));

        mockMvc.perform(get(
                        "/api/report-files/{fileId}/validation-runs/{validationRunId}/findings",
                        fileId,
                        validationRunId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.count").value(1))
                .andExpect(jsonPath("$.data[0].code").value("INVALID_STRUCTURE"));

        verify(reportFileService).listFindingsByValidationRun(fileId, validationRunId);
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

    private UploadedFileResponse uploadedFileResponse() {
        return new UploadedFileResponse(
                UUID.randomUUID(),
                "report.xlsx",
                "stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                "STORED",
                "analyst01",
                null,
                null
        );
    }

    private UploadedFileValidationRunResponse validationRunResponse(UUID fileId) {
        return new UploadedFileValidationRunResponse(
                UUID.randomUUID(),
                fileId,
                "FAILED",
                "UPLOAD",
                "Uploaded file validation found issues",
                "analyst01",
                LocalDateTime.now()
        );
    }

    private UploadedFileFindingResponse findingResponse(UUID fileId, UUID validationRunId) {
        return new UploadedFileFindingResponse(
                UUID.randomUUID(),
                validationRunId,
                fileId,
                "ERROR",
                "FILE_STRUCTURE",
                "INVALID_STRUCTURE",
                "Invalid file structure",
                "Hoja1",
                null,
                null,
                null,
                null,
                "Expected structure",
                "Invalid structure",
                LocalDateTime.now()
        );
    }

    private UploadedFile uploadedFile() {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                7L,
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
