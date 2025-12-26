package org.knullci.knull.persistence.config;

import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		if (userRepository.count() == 0) {
			// Create default admin user
			User adminUser = new User(
					null, // id
					"knull", // username
					"admin@knullci.local", // email
					passwordEncoder.encode("knull"), // password
					"Knull Admin", // displayName
					Role.ADMIN, // role - ADMIN by default
					Set.of(), // additionalPermissions
					true, // active
					false, // accountLocked
					LocalDateTime.now(), // createdAt
					LocalDateTime.now(), // updatedAt
					null // lastLoginAt
			);

			userRepository.save(adminUser);

			System.out.println("Default admin user created: knull (password: knull)");
		}
	}

}
