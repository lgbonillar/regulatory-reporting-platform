package dev.lgbonillar.regreporting.upload.validation;

import dev.lgbonillar.regreporting.modules.demo.validation.DemoUploadedFileValidator;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadedFileValidatorRegistryTest {

    @Test
    void getDefaultValidatorReturnsDemoValidator() {
        UploadedFileValidator demoValidator = new StubUploadedFileValidator(DemoUploadedFileValidator.VALIDATOR_CODE);
        UploadedFileValidatorRegistry registry = new UploadedFileValidatorRegistry(List.of(
                new StubUploadedFileValidator("CNBV_UPLOADED_FILE"),
                demoValidator
        ));

        UploadedFileValidator result = registry.getDefaultValidator();

        assertThat(result).isSameAs(demoValidator);
    }

    @Test
    void getValidatorReturnsValidatorByCode() {
        UploadedFileValidator cnbvValidator = new StubUploadedFileValidator("CNBV_UPLOADED_FILE");
        UploadedFileValidatorRegistry registry = new UploadedFileValidatorRegistry(List.of(
                new StubUploadedFileValidator(DemoUploadedFileValidator.VALIDATOR_CODE),
                cnbvValidator
        ));

        UploadedFileValidator result = registry.getValidator("CNBV_UPLOADED_FILE");

        assertThat(result).isSameAs(cnbvValidator);
    }

    @Test
    void getValidatorThrowsWhenValidatorIsNotConfigured() {
        UploadedFileValidatorRegistry registry = new UploadedFileValidatorRegistry(List.of(
                new StubUploadedFileValidator(DemoUploadedFileValidator.VALIDATOR_CODE)
        ));

        assertThatThrownBy(() -> registry.getValidator("CNBV_UPLOADED_FILE"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Uploaded file validator is not configured: CNBV_UPLOADED_FILE");
    }

    @Test
    void getDefaultValidatorThrowsWhenDemoValidatorIsNotConfigured() {
        UploadedFileValidatorRegistry registry = new UploadedFileValidatorRegistry(List.of(
                new StubUploadedFileValidator("CNBV_UPLOADED_FILE")
        ));

        assertThatThrownBy(registry::getDefaultValidator)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Default uploaded file validator is not configured");
    }

    private record StubUploadedFileValidator(String code) implements UploadedFileValidator {

        @Override
        public UploadedFileValidationResult validate(UploadedFile uploadedFile) {
            return UploadedFileValidationResult.passed();
        }
    }
}
