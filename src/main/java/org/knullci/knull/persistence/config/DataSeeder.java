package org.knullci.knull.persistence.config;

import org.knullci.knull.persistence.entity.User;
import org.knullci.knull.persistence.repository.JpaUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
	
	private final JpaUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public void run(String... args) throws Exception {
		if(userRepository.count() == 0) {
			User user = new User();
			
			user.setUsername("knull");
			user.setPassword(passwordEncoder.encode("knullci"));
			
			userRepository.save(user);
			
			System.out.println("User created");
		}
	}

}
