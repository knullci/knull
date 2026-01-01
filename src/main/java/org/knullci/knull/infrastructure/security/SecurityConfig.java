package org.knullci.knull.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf
				.ignoringRequestMatchers("/api/v1/webhook/github", "/builds/*/cancel"))
				.authorizeHttpRequests(auth -> auth
						// Public endpoints
						.requestMatchers(
								"/api/v1/webhook/github",
								"/login",
								"/setup",
								"/css/**",
								"/static/**",
								"/images/**",
								"/js/**")
						.permitAll()

						// Admin-only endpoints
						.requestMatchers(
								"/admin/**",
								"/users/**",
								"/settings/**")
						.hasRole("ADMIN")

						// Developer and Admin endpoints
						.requestMatchers(
								"/jobs/new",
								"/jobs/*/edit",
								"/jobs/*/delete",
								"/jobs/*/build",
								"/credentials/new",
								"/credentials/*/edit",
								"/credentials/*/delete",
								"/secrets/new",
								"/secrets/*/edit",
								"/secrets/*/delete")
						.hasAnyRole("ADMIN", "DEVELOPER")

						// All authenticated users can view
						.requestMatchers(
								"/",
								"/jobs",
								"/jobs/*",
								"/builds/**",
								"/credentials",
								"/secrets")
						.hasAnyRole("ADMIN", "DEVELOPER", "VIEWER")

						// Any other request requires authentication
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/", true)
						.permitAll())
				.logout(logout -> logout
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.exceptionHandling(exception -> exception
						.accessDeniedPage("/access-denied"));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncode() {
		return new BCryptPasswordEncoder();
	}
}
