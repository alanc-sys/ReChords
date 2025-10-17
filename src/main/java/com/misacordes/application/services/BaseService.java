package com.misacordes.application.services;

import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseService {

    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return (User) authentication.getPrincipal();
    }

    protected void verifyAdmin() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("No tienes permisos de administrador");
        }
    }

    protected void verifyOwnershipOrAdmin(Long resourceOwnerId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getId() != resourceOwnerId) {
            throw new RuntimeException("No tienes permisos para acceder a este recurso");
        }
    }

    protected void verifyAuthenticated() {
        getCurrentUser();
    }
}
