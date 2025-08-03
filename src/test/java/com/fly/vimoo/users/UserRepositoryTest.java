package com.fly.vimoo.users;

import com.fly.vimoo.user.domain.entity.User;
import com.fly.vimoo.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	private User testUser;
	private User adminUser;
	private User inactiveUser;


	@BeforeEach
	void setUp() {
		testUser = User.builder()
				.email("test@example.com")
				.username("testuser")
				.password("hashedPassword123")
				.firstName("John")
				.lastName("Wick")
				.isActive(true)
				.emailVerified(true)
				.isPublic(true)
				.role(User.Role.USER)
				.build();

		adminUser = User.builder()
				.email("admin@example.com")
				.username("adminuser")
				.password("hashedPassword123")
				.firstName("Admin")
				.lastName("Wick")
				.isActive(true)
				.emailVerified(true)
				.isPublic(true)
				.role(User.Role.ADMIN)
				.build();

		inactiveUser = User.builder()
				.email("inactive@example.com")
				.username("inactiveuser")
				.password("hashedPassword123")
				.isActive(false)
				.emailVerified(true)
				.isPublic(false)
				.role(User.Role.USER)
				.build();

		//Save in DB
		entityManager.persistAndFlush(testUser);
		entityManager.persistAndFlush(adminUser);
		entityManager.persistAndFlush(inactiveUser);
	}


	@Test
	void findByEmail_withExistingEmail_shouldReturnUser() {
		Optional<User> userFound = userRepository.findByEmail("test@example.com");
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getEmail()).isEqualTo("test@example.com");
		assertThat(userFound.get().getUsername()).isEqualTo("test@example.com");
	}


	@Test
	void findByEmail_withNonExistingEmail_shouldReturnEmpty() {
		Optional<User> userFound = userRepository.findByEmail("nonexisting@example");
		assertThat(userFound).isEmpty();
	}

	@Test
	void findByUsername_withExistingUsername_shouldReturnUser() {
		Optional<User> userFound = userRepository.findByUsername("testuser");
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getUsername_()).isEqualTo("testuser");
	}


	@Test
	void findByEmailIgnoreCase_withDifferentCase_shouldReturnUser() {
		Optional<User> userFound = userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM");
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getEmail()).isEqualTo("test@example.com");
	}

	@Test
	void findByUserNameIgnoreCase_withDifferentCase_shouldReturnUser() {
		Optional<User> userFound = userRepository.findByUsernameIgnoreCase("TESTUSER");
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getUsername_()).isEqualTo("testuser");
	}


	//Tokens test

	@Test
	void findByEmailVerificationToken_withValidToken_shouldReturnUser() {

		String token = "verification-token-123";
		testUser.setEmailVerificationToken(token, LocalDateTime.now().plusDays(1));
		entityManager.persistAndFlush(testUser);

		Optional<User> userFound = userRepository.findByEmailVerificationToken(token);
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getEmailVerificationToken()).isEqualTo(token);
	}

	@Test
	void findByValidEmailVerificationToken_withValidToken_shouldReturnUser() {

		String token = "verification-token-123";
		testUser.setEmailVerificationToken(token, LocalDateTime.now().plusDays(1));
		entityManager.persistAndFlush(testUser);

		Optional<User> userFound = userRepository.findByValidEmailVerificationToken(token, LocalDateTime.now());
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getEmailVerificationToken()).isEqualTo(token);
	}

	//Check token exist.
	@Test
	void findByPasswordResetToken_withValidToken_shouldReturnUser() {

		String token = "reset-token-123";
		testUser.setPasswordResetToken(token, LocalDateTime.now().plusDays(1));
		entityManager.persistAndFlush(testUser);

		Optional<User> userFound = userRepository.findByPasswordResetToken(token);
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getPasswordResetToken()).isEqualTo(token);
	}

	//Check is token valid
	@Test
	void findByValidPasswordResetToken_withValidToken_shouldReturnUser() {

		String token = "reset-token-123";
		testUser.setPasswordResetToken(token, LocalDateTime.now().plusDays(1));
		entityManager.persistAndFlush(testUser);

		Optional<User> userFound = userRepository.findByValidPasswordResetToken(token, LocalDateTime.now());
		assertThat(userFound).isPresent();
		assertThat(userFound.get().getPasswordResetToken()).isEqualTo(token);
	}

	//TODO : check other unit test

}
