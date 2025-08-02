package com.fly.vimoo.users;

import com.fly.vimoo.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class UserEntityTest {


	private User user;

	@BeforeEach
	void setUp(){
		user = User.builder()
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
	}

	@Test
	void getUsername_shouldReturnEmail() {
		assertThat(user.getUsername()).isEqualTo("test@example.com");
	}

	@Test
	void getPassword_shouldReturnHashedPassword() {
		assertThat(user.getPassword()).isEqualTo("hashedPassword123");
	}

	@Test
	void getAuthorities_withUserRole_shouldReturnUserRole() {
		Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
		assertThat(authorities).hasSize(1);
		assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");

	}

	@Test
	void isEnabled_ShouldReturnTrue() {
		assertThat(user.isEnabled()).isTrue();
	}


	@Test
	void isEnabledWith_InactiveUser_ShouldReturnFalse() {
		user.setIsActive(false);
		assertThat(user.isEnabled()).isFalse();
	}

	@Test
	void isEnabledWith_EmailNotVerified_ShouldReturnFalse() {
		user.setEmailVerified(false);
		assertThat(user.isEnabled()).isFalse();
	}

	@Test
	void isAccountNonExpired_WithActiveUser_ShouldReturnTrue() {
		assertThat(user.isAccountNonExpired()).isTrue();
	}

	@Test
	void isAccountNonLocked_WithActiveUser_ShouldReturnTrue() {
		assertThat(user.isAccountNonExpired()).isTrue();
	}



	//Test about tokens

	@Test
	void isEmailVerificationTokenValid_WithValidToken_ShouldReturnTrue() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
		user.setEmailVerificationToken("token123");
		user.setEmailVerificationExpires(futureTime);
		assertThat(user.isEmailVerificationTokenValid()).isTrue();
	}

	@Test
	void isEmailVerificationTokenValid_WithExpiredToken_ShouldReturnFalse() {
		LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
		user.setEmailVerificationToken("token123");
		user.setEmailVerificationExpires(pastTime);
		assertThat(user.isEmailVerificationTokenValid()).isFalse();
	}

	@Test
	void isEmailVerificationTokenValid_WithoutToken_ShouldReturnFalse() {
		assertThat(user.isEmailVerificationTokenValid()).isFalse();
	}


	@Test
	void isPasswordResetTokenValid_WithValidToken_ShouldReturnTrue() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
		user.setPasswordResetToken("resettoken123");
		user.setPasswordResetExpires(futureTime);
		assertThat(user.isPasswordResetTokenValid()).isTrue();
	}

	@Test
	void isPasswordResetTokenValid_WithExpiredToken_ShouldReturnFalse() {
		LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
		user.setPasswordResetToken("token123");
		user.setPasswordResetExpires(pastTime);
		assertThat(user.isPasswordResetTokenValid()).isFalse();
	}

	@Test
	void isPasswordResetTokenValid_WithoutToken_ShouldReturnFalse() {
		assertThat(user.isPasswordResetTokenValid()).isFalse();
	}


	@Test
	void verifyEmail_shouldSetEmailVerifiedAndClearToken() {
		user.setEmailVerified(false);
		user.setEmailVerificationToken("token123");
		user.setEmailVerificationExpires(LocalDateTime.now().plusHours(1));

		user.verifyEmail();

		assertThat(user.getEmailVerified()).isTrue();
		assertThat(user.getEmailVerificationToken()).isNull();
		assertThat(user.getEmailVerificationExpires()).isNull();
	}
}
