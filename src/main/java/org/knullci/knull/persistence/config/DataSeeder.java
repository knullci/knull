package org.knullci.knull.persistence.config;

import org.knullci.knull.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Data Seeder for initial application setup.
 * 
 * This component checks if the application needs initial setup (no users exist)
 * and logs appropriate messages. The actual admin user creation is handled
 * by the SetupController and SetupAdminCommandHandler when accessed via the
 * /setup endpoint.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

	private final UserRepository userRepository;

	@Override
	public void run(String... args) throws Exception {
		long userCount = userRepository.count();
		if (userCount == 0) {
			logger.info("╔═══════════════════════════════════════════════════════════════════╗");
			logger.info("║                    KNULL CI/CD FIRST TIME SETUP                   ║");
			logger.info("╠═══════════════════════════════════════════════════════════════════╣");
			logger.info("║  No users found in the database.                                  ║");
			logger.info("║  Please navigate to /setup to create the admin user.             ║");
			logger.info("╚═══════════════════════════════════════════════════════════════════╝");
		} else {
			logger.info("Knull CI/CD started successfully. {} user(s) found.", userCount);
		}
	}
}
