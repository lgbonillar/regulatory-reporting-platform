package dev.lgbonillar.regreporting.upload.domain;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uploaded_file_findings")
@Getter
@NoArgsConstructor
public class UploadedFileFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validation_run_id", nullable = false)
    private UploadedFileValidationRun validationRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_file_id", nullable = false)
    private UploadedFile uploadedFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingFindingSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingFindingScope scope;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String message;

    @Column(name = "sheet_name")
    private String sheetName;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "column_name")
    private String columnName;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "rejected_value")
    private String rejectedValue;

    @Column(name = "expected_value")
    private String expectedValue;

    @Column(name = "actual_value")
    private String actualValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UploadedFileFinding(
            UploadedFileValidationRun validationRun,
            UploadedFile uploadedFile,
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
        this.validationRun = validationRun;
        this.uploadedFile = uploadedFile;
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