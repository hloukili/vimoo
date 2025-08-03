package com.fly.vimoo.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


/**
 * Entité User - Utilisateur de l'application.
 * Implémente UserDetails pour pouvoir utiliser Spring Security.
 **/


@Entity
@Table(name = "users", indexes = {
		@Index(name = "idx_user_email", columnList = "email"),
		@Index(name = "idx_user_username", columnList = "username"),
		@Index(name = "idx_user_active", columnList = "is_active"),
		@Index(name = "idx_user_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "email", unique = true, nullable = false, length = 255)
	@Email(message = "Format email invalide !")
	private String email;

	@Column(name = "username", unique = true, nullable = false, length = 255)
	@NotBlank(message = "Username requis")
	@Size(min = 3, max = 50, message = "Username doit contenir entre 3 et 50 caractères")
	private String username;

	/**
	 * Mot de passe haché (BCrypt)
	 **/
	@Column(name = "password_hash", nullable = false, length = 255)
	@NotBlank(message = "Mot de passe requis")
	private String password;

	@Column(name = "first_name", length = 100)
	@Size(max = 100, message = "Prénom maximum 100 caractères")
	private String firstName;

	@Column(name = "last_name", length = 100)
	@Size(max = 100, message = "Nom maximum 100 caractères")
	private String lastName;

	/**
	 * URL de l'avatar utilisateur (S3 ou chemin relatif)
	 */
	@Column(name = "avatar_url", length = 500)
	private String avatarUrl;

	@Column(name = "bio", length = 500)
	@Size(max = 500, message = "Bio maximum 500 caractères")
	private String bio;

	/**
	 * Profil public ou privé.
	 */
	@Column(name = "is_public", nullable = false)
	@Builder.Default
	private Boolean isPublic = true;

	/**
	 * Email vérifié ou non..
	 */
	@Column(name = "email_verified", nullable = false)
	@Builder.Default
	private Boolean emailVerified = false;


	/**
	 * Token de vérification d'email
	 */
	@Column(name = "email_verification_token", length = 255)
	private String emailVerificationToken;

	/**
	 * Date d'expiration du token de vérification d'email
	 */
	@Column(name = "email_verification_expires")
	private LocalDateTime emailVerificationExpires;


	/**
	 * Token de réinitialisation de mot de passe
	 */
	@Column(name = "password_reset_token", length = 255)
	private String passwordResetToken;


	/**
	 * Date d'expiration du token de réinitialisation
	 */
	@Column(name = "password_reset_expires")
	private LocalDateTime passwordResetExpires;


	/**
	 * Compte actif ou désactivé
	 */
	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;


	/**
	 * Rôle de l'utilisateur
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	@Builder.Default
	private Role role = Role.USER;


	/**
	 * Date de dernière connexion
	 */
	@Column(name = "last_login")
	private LocalDateTime lastLogin;


	/**
	 * Date de dernière modification
	 */
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;


	/**
	 * Date de création du compte
	 */
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;


	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return password;
	}


	public String getUsername_() {
		return username;
	}


	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return isActive;
	}

	@Override
	public boolean isAccountNonLocked() {
		return isActive;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return isActive && emailVerified;
	}

	// ===== MÉTHODES UTILITAIRES =====

	public String getFullName() {
		if (firstName != null && lastName != null) {
			return firstName.trim() + " " + lastName.trim();
		} else if (firstName != null) {
			return firstName.trim();
		} else if (lastName != null) {
			return lastName.trim();
		}
		return username;
	}

	public String getDisplayName() {
		String fullName = getFullName();
		return fullName.equals(username) ? username : fullName;
	}

	/**
	 * Vérifie si le token de réinitialisation de mot de passe est valide
	 */
	public boolean isPasswordResetTokenValid() {
		return passwordResetToken != null
				&& passwordResetExpires != null
				&& passwordResetExpires.isAfter(LocalDateTime.now());
	}


	/**
	 * Vérifie si le token de vérification d'email est valide
	 */
	public boolean isEmailVerificationTokenValid() {
		return emailVerificationToken != null
				&& emailVerificationExpires != null
				&& emailVerificationExpires.isAfter(LocalDateTime.now());
	}


	public boolean isAdmin() {
		return Role.ADMIN.equals(role);
	}

	/**
	 * Vérifie si l'utilisateur peut être follow.
	 */
	public boolean canBeFollowed() {
		return isPublic && isActive && emailVerified;
	}


	public void updateLastLogin() {
		this.lastLogin = LocalDateTime.now();
	}


	/**
	 * Active la vérification d'email
	 */
	public void verifyEmail() {
		this.emailVerified = true;
		this.emailVerificationToken = null;
		this.emailVerificationExpires = null;
	}

	/**
	 * Définit un token de vérification d'email avec expiration
	 */
	public void setEmailVerificationToken(String token, LocalDateTime expires) {
		this.emailVerificationToken = token;
		this.emailVerificationExpires = expires;
	}


	/**
	 * Définit un token de reset password avec expiration
	 */
	public void setPasswordResetToken(String token, LocalDateTime expires) {
		this.passwordResetToken = token;
		this.passwordResetExpires = expires;
	}


	public void clearPasswordResetToken() {
		this.passwordResetToken = null;
		this.passwordResetExpires = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;
		User user = (User) o;
		return id != null && id.equals(user.getId());
	}

	/**
	 * HashCode basé sur l'ID uniquement
	 */
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}


	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", email='" + email + '\'' +
				", username='" + username + '\'' +
				", role=" + role +
				", isActive=" + isActive +
				", emailVerified=" + emailVerified +
				", createdAt=" + createdAt +
				'}';
	}


	// ===== ÉNUMÉRATION ROLE =====

	/**
	 * Rôles disponibles dans l'application
	 */
	public enum Role {
		/**
		 * Utilisateur standard
		 */
		USER,

		/**
		 * Administrateur avec tous les privilèges
		 */
		ADMIN,

		/**
		 * Modérateur (pour futures fonctionnalités)
		 */
		MODERATOR
	}


}
