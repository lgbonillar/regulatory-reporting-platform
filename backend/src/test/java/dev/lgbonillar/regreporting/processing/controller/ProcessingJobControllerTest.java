package dev.lgbonillar.regreporting.processing.controller;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobService;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobFindingResponse;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessingJobController.class)
class ProcessingJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessingJobService processingJobService;

    @Test
    void listProcessingJobsReturnsJobs() throws Exception {
        ProcessingJobResponse response = response();

        when(processingJobService.listProcessingJobs("analyst01")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/processing-jobs")
                        .param("username", "analyst01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.count").value(1))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].originalFilename").value("report.xlsx"))
                .andExpect(jsonPath("$.data[0].jobStatus").value("PENDING_EXECUTION"));

        verify(processingJobService).listProcessingJobs("analyst01");
    }

    @Test
    void getProcessingJobReturnsJob() throws Exception {
        UUID jobId = UUID.randomUUID();

        when(processingJobService.getProcessingJob(jobId)).thenReturn(response());

        mockMvc.perform(get("/api/processing-jobs/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalFilename").value("report.xlsx"))
                .andExpect(jsonPath("$.data.jobStatus").value("PENDING_EXECUTION"));

        verify(processingJobService).getProcessingJob(jobId);
    }

    @Test
    void startProcessingReturnsUpdatedJob() throws Exception {
        UUID jobId = UUID.randomUUID();

        when(processingJobService.startProcessing(jobId)).thenReturn(response("PROCESSING"));

        mockMvc.perform(post("/api/processing-jobs/{jobId}/start", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobStatus").value("PROCESSING"));

        verify(processingJobService).startProcessing(jobId);
    }

    @Test
    void failProcessingPassesReasonToService() throws Exception {
        UUID jobId = UUID.randomUUID();

        when(processingJobService.failProcessing(jobId, "invalid layout"))
                .thenReturn(response("PROCESSING_FAILED"));

        mockMvc.perform(post("/api/processing-jobs/{jobId}/fail", jobId)
                        .contentType("application/json")
                        .content("""
                                  {
                                    "reason": "invalid layout"
                                  }
                                  """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobStatus").value("PROCESSING_FAILED"));

        verify(processingJobService).failProcessing(jobId, "invalid layout");
    }

    @Test
    void approveReturnsUpdatedJob() throws Exception {
        UUID jobId = UUID.randomUUID();

        when(processingJobService.approve(jobId)).thenReturn(response("APPROVED"));

        mockMvc.perform(post("/api/processing-jobs/{jobId}/approve", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobStatus").value("APPROVED"));

        verify(processingJobService).approve(jobId);
    }

    @Test
    void rejectPassesReasonToService() throws Exception {
        UUID jobId = UUID.randomUUID();

        when(processingJobService.reject(jobId, "totals do not match"))
                .thenReturn(response("REJECTED"));

        mockMvc.perform(post("/api/processing-jobs/{jobId}/reject", jobId)
                        .contentType("application/json")
                        .content("""
                                  {
                                    "reason": "totals do not match"
                                  }
                                  """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobStatus").value("REJECTED"));

        verify(processingJobService).reject(jobId, "totals do not match");
    }

    @Test
    void getProcessingJobHistoryReturnsHistory() throws Exception {
        UUID jobId = UUID.randomUUID();
        ProcessingJobStatusHistoryResponse history = new ProcessingJobStatusHistoryResponse(
                UUID.randomUUID(),
                "PENDING_EXECUTION",
                "PROCESSING",
                "USER",
                "admin01",
                "Administrator started processing",
                null
        );

        when(processingJobService.getProcessingJobHistory(jobId)).thenReturn(List.of(history));

        mockMvc.perform(get("/api/processing-jobs/{jobId}/history", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.count").value(1))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].previousStatus").value("PENDING_EXECUTION"))
                .andExpect(jsonPath("$.data[0].newStatus").value("PROCESSING"));

        verify(processingJobService).getProcessingJobHistory(jobId);
    }

    @Test
    void getProcessingJobFindingsReturnsFindings() throws Exception {
        UUID jobId = UUID.randomUUID();
        ProcessingJobFindingResponse finding = new ProcessingJobFindingResponse(
                UUID.randomUUID(),
                "ERROR",
                "ROW_DATA",
                "INVALID_DATA_TYPE",
                "Amount must be numeric",
                "Clients",
                14,
                "amount",
                "amount",
                "ABC",
                "numeric",
                "ABC",
                null
        );

        when(processingJobService.getProcessingJobFindings(jobId)).thenReturn(List.of(finding));

        mockMvc.perform(get("/api/processing-jobs/{jobId}/findings", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.count").value(1))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].severity").value("ERROR"))
                .andExpect(jsonPath("$.data[0].scope").value("ROW_DATA"))
                .andExpect(jsonPath("$.data[0].code").value("INVALID_DATA_TYPE"));

        verify(processingJobService).getProcessingJobFindings(jobId);
    }

    private ProcessingJobResponse response() {
        return response("PENDING_EXECUTION");
    }

    private ProcessingJobResponse response(String jobStatus) {
        return new ProcessingJobResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "report.xlsx",
                "STORED",
                jobStatus,
                "File uploaded",
                "analyst01",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

}
