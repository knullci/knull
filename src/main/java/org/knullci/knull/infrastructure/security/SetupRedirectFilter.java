package org.knullci.knull.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.knullci.knull.application.interfaces.SetupAdminCommandHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filter that redirects users to the setup page when no users exist in the
 * database.
 * This ensures that the first-time setup flow is followed before using the
 * application.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class SetupRedirectFilter extends OncePerRequestFilter {

    private final SetupAdminCommandHandler setupAdminCommandHandler;

    // Paths that should be exempt from setup redirect
    private static final Set<String> EXEMPT_PATHS = Set.of(
            "/setup",
            "/css",
            "/js",
            "/images",
            "/static",
            "/favicon.ico",
            "/error");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Check if path is exempt from redirect
        boolean isExempt = EXEMPT_PATHS.stream()
                .anyMatch(path -> requestPath.equals(path) || requestPath.startsWith(path + "/"));

        // If setup is required and path is not exempt, redirect to setup
        if (!isExempt && setupAdminCommandHandler.isSetupRequired()) {
            response.sendRedirect(request.getContextPath() + "/setup");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
