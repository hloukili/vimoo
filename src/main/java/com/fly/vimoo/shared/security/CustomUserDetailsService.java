package com.fly.vimoo.shared.security;


import com.fly.vimoo.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 1. Charger un utilisateur par son email (username)
 * 2. Retourner les détails nécessaires à Spring Security
 * 3. Gérer les cas d'utilisateurs non trouvés.
 */


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {


	private final UserRepository userRepository;


	/**
	 * Call method when :
	 * 1. Login (DaoAuthenticationProvider)
	 * 2. JWT (JwtAuthenticationFilter) for every request.
	 * 3. Refresh token
	 * * @param username
	 * * @return UserDetails (roles/permissions)
	 * * @throws UsernameNotFoundException if user not found
	 */

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Loading user by username {}", username);

		return userRepository.findByEmail(username)
				.orElseThrow(() -> {
					log.warn("User not found with username: {}", username);
					return new UsernameNotFoundException("User not found with username: " + username);

				});
	}

	/**
	 * Load user by ID (UUID)
	 */
	@Transactional(readOnly = true)
	public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
		log.debug("Loading user by ID: {}", userId);

		/*return userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("User not found with ID: {}", userId);
					return new UsernameNotFoundException("User not found with ID: " + userId);
				});*/

		return null;
	}
}
