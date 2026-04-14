package ua.edu.ukma.zlagodabackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.edu.ukma.zlagodabackend.annotation.ValidPhone;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhone, String> {

    private static final String PHONE_PATTERN = "^\\+[0-9]{1,12}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank обробляє null/порожній рядок
        }
        return value.matches(PHONE_PATTERN);
    }
}
