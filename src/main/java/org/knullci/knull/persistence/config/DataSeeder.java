package org.knullci.knull.persistence.config;

import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public void run(String... args) throws Exception {
		if(userRepository.count() == 0) {
			User user = new User("knull", passwordEncoder.encode("knull"));

            userRepository.save(user);
			
			System.out.println("User created");
		}
	}

}
