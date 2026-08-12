package ch.admin.bit.jeap.opensearch.registry.verifier;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationResult {
    private static final ValidationResult OK = new ValidationResult(
            true, Collections.emptyList(), Collections.emptyList());
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;

    public static ValidationResult ok() {
        return OK;
    }

    public static ValidationResult fail(String error) {
        return new ValidationResult(false, Collections.singletonList(error), Collections.emptyList());
    }

    public static ValidationResult warning(String warning) {
        return new ValidationResult(true, Collections.emptyList(), Collections.singletonList(warning));
    }

    public static ValidationResult merge(ValidationResult... results) {
        return Arrays.stream(results).reduce(ok(), ValidationResult::merge);
    }

    private static ValidationResult merge(ValidationResult r1, ValidationResult r2) {
        List<String> errors = Stream.concat(r1.getErrors().stream(), r2.getErrors().stream())
                .collect(Collectors.toList());
        List<String> warnings = Stream.concat(r1.getWarnings().stream(), r2.getWarnings().stream())
                .collect(Collectors.toList());
        return new ValidationResult(r1.isValid() && r2.isValid(), errors, warnings);
    }
}
