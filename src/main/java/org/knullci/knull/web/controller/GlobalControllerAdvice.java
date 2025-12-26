package org.knullci.knull.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.knullci.knull.domain.model.KnullUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice that adds common model attributes to all
 * controllers, including user information for RBAC.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Adds the current request URI to the model for all requests.
     * This is needed because Thymeleaf 3.1+ no longer exposes #request by default.
     *
     * @param request the HTTP servlet request
     * @return the current request URI
     */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Adds the current authenticated user to the model.
     */
    @ModelAttribute("currentUser")
    public KnullUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof KnullUserDetails) {
            return (KnullUserDetails) auth.getPrincipal();
        }
        return null;
    }

    /**
     * Checks if the current user is an admin.
     */
    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        KnullUserDetails user = currentUser();
        return user != null && user.getRole() != null &&
                user.getRole().name().equals("ADMIN");
    }

    /**
     * Checks if the current user is a developer or admin.
     */
    @ModelAttribute("canEdit")
    public boolean canEdit() {
        KnullUserDetails user = currentUser();
        if (user == null || user.getRole() == null)
            return false;
        String role = user.getRole().name();
        return role.equals("ADMIN") || role.equals("DEVELOPER");
    }
}
