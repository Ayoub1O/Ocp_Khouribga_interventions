package com.pfe.itsm.users.service;

import com.pfe.itsm.common.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {

    public void validate(String password) {
        if (password == null || password.length() < 12) {
            throw new BusinessException("Le mot de passe doit contenir au moins 12 caracteres.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins une lettre majuscule.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins une lettre minuscule.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins un chiffre.");
        }
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins un caractere special.");
        }
    }
}

