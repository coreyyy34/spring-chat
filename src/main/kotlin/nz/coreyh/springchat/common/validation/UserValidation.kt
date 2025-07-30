package nz.coreyh.springchat.common.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.notBlank
import nz.coreyh.springchat.domain.model.dto.AuthLoginRequest
import nz.coreyh.springchat.domain.model.dto.AuthRegisterRequest

object UserValidation {
    private const val USERNAME_MIN_LENGTH = 2;
    private const val USERNAME_MAX_LENGTH = 32;
    private const val PASSWORD_MIN_LENGTH = 8
    private const val PASSWORD_MAX_LENGTH = 64

    private const val USERNAME_BLANK = "Username cannot be blank"
    private const val USERNAME_LENGTH = "Username must be between $USERNAME_MIN_LENGTH and " +
            "$USERNAME_MAX_LENGTH characters"
    const val USERNAME_IN_USE = "Username is already in use"

    private const val PASSWORD_BLANK = "Password cannot be blank"
    private const val PASSWORD_LENGTH = "Password must be between $PASSWORD_MIN_LENGTH and " +
            "$PASSWORD_MAX_LENGTH characters"
    private const val PASSWORD_MISMATCH = "Passwords do not match"

    val REGISTER_VALIDATION = Validation<nz.coreyh.springchat.domain.model.dto.AuthRegisterRequest> {
        nz.coreyh.springchat.domain.model.dto.AuthRegisterRequest::username {
            notBlank().hint(USERNAME_BLANK)
            minLength(USERNAME_MIN_LENGTH).hint(USERNAME_LENGTH)
            maxLength(USERNAME_MAX_LENGTH).hint(USERNAME_LENGTH)
        }
        nz.coreyh.springchat.domain.model.dto.AuthRegisterRequest::password {
            notBlank().hint(PASSWORD_BLANK)
            minLength(PASSWORD_MIN_LENGTH).hint(PASSWORD_LENGTH)
            maxLength(PASSWORD_MAX_LENGTH).hint(PASSWORD_LENGTH)
        }
        dynamic {
            constrain(hint = PASSWORD_MISMATCH) {
                it.password == it.confirmPassword
            }
        }
    }

    val LOGIN_VALIDATION = Validation {
        nz.coreyh.springchat.domain.model.dto.AuthLoginRequest::username {
            notBlank().hint(USERNAME_BLANK)
        }
        nz.coreyh.springchat.domain.model.dto.AuthLoginRequest::password {
            notBlank().hint(PASSWORD_BLANK)
        }
    }
}