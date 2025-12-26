package org.knullci.knull.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice that adds common model attributes to all
 * controllers.
 * This is used to provide the current request path for navbar highlighting.
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
}
