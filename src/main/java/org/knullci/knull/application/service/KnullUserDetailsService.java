package org.knullci.knull.application.service;

import org.knullci.knull.domain.model.KnullUserDetails;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class KnullUserDetailsService implements UserDetailsService {
	
	private final static Logger logger = LoggerFactory.getLogger(KnullUserDetailsService.class);

	private final UserRepository userRepository;
	
	public KnullUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.info("Load username by username: {}", username);

		User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		
		return new KnullUserDetails(user);
	}

}
