package dev.lgbonillar.regreporting.processing.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_job_findings")
@Getter
@NoArgsConstructor
public class ProcessingJobFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processing_job_id", nullable = false)
    private ProcessingJob processingJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private ProcessingFindingSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 40)
    private ProcessingFindingScope scope;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "sheet_name", length = 150)
    private String sheetName;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "column_name", length = 150)
    private String columnName;

    @Column(name = "field_name", length = 150)
    private String fieldName;

    @Column(name = "rejected_value", length = 1000)
    private String rejectedValue;

    @Column(name = "expected_value", length = 1000)
    private String expectedValue;

    @Column(name = "actual_value", length = 1000)
    private String actualValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProcessingJobFinding(
            ProcessingJob processingJob,
            ProcessingFindingSeverity severity,
            ProcessingFindingScope scope,
            String code,
            String message,
            String sheetName,
            Integer rowNumber,
            String columnName,
            String fieldName,
            String rejectedValue,
            String expectedValue,
            String actualValue
    ) {
        this.processingJob = processingJob;
        this.severity = severity;
        this.scope = scope;
        this.code = code;
        this.message = message;
        this.sheetName = sheetName;
        this.rowNumber = rowNumber;
        this.columnName = columnName;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }
}
