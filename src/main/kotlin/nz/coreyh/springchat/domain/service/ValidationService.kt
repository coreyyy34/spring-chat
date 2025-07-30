package nz.coreyh.springchat.domain.service

import io.konform.validation.Validation
import nz.coreyh.springchat.common.exception.ValidationException.Companion.throwValidationException
import org.springframework.stereotype.Service

@Service
class ValidationService {

    fun <T> validate(dto: T, validation: Validation<T>) {
        val result = validation.validate(dto)
        if (!result.isValid) {
            val errors = result.errors
                .groupBy { it.path.segments.last().pathString.removePrefix(".") }
                .map { (fieldName, errorList) -> fieldName to errorList.first().message }
                .toMap()
            if (errors.isNotEmpty())
                throwValidationException(errors)
        }
    }
}