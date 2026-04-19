package ua.edu.ukma.zlagodabackend.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ua.edu.ukma.zlagodabackend.validation.PhoneNumberValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Невалідний номер телефону. Допустимий формат: +XXXXXXXXXXXX (1-12 цифр)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
