package dev.lgbonillar.regreporting.upload.validation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UploadedFileValidatorRegistry {

    private static final String DEFAULT_VALIDATOR_CODE = "DEMO_UPLOADED_FILE";

    private final List<UploadedFileValidator> validators;

    public UploadedFileValidatorRegistry(List<UploadedFileValidator> validators) {
        this.validators = validators;
    }

    public UploadedFileValidator getDefaultValidator() {
        return findValidator(DEFAULT_VALIDATOR_CODE)
                .orElseThrow(() -> new IllegalStateException("Default uploaded file validator is not configured"));
    }

    public UploadedFileValidator getValidator(String code) {
        return findValidator(code)
                .orElseThrow(() -> new IllegalStateException("Uploaded file validator is not configured: " + code));
    }

    private Optional<UploadedFileValidator> findValidator(String code) {
        return validators.stream()
                .filter(validator -> validator.code().equals(code))
                .findFirst();
    }
}
