package dev.lgbonillar.regreporting.upload.validation;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;

public interface UploadedFileValidator {

    String code();

    UploadedFileValidationResult validate(UploadedFile uploadedFile);
}
