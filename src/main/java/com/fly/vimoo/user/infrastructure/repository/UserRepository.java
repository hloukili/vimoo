package com.fly.vimoo.user.infrastructure.repository;

import com.fly.vimoo.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	/**
	* @param email (insensitive)
	**/
	Optional<User> findByEmailIgnoreCase(String email);

	/**
	 * @param username (insensitive)
	 **/
	Optional<User> findByUsernameIgnoreCase(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	/**
	 * @param token verification
	 **/
	Optional<User> findByEmailVerificationToken(String token);

	/**
	 * @param token verification
	 **/
	Optional<User> findByPasswordResetToken(String token);


	/**
	 * Find User by Token check email valid (no expires)
	 * @param token Verification Token
	 * @param now current date
	 **/
	@Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.emailVerificationExpires > :now")
	Optional<User> findByValidEmailVerificationToken(@Param("token") String token,
													 @Param("now") LocalDateTime now);


	/**
	 * Find User by Token reset password valid (no expires)
	 * @param token Verification Token
	 * @param now current date
	 **/
	@Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpires > :now")
	Optional<User> findByValidPasswordResetToken(@Param("token") String token,
													 @Param("now") LocalDateTime now);



	// =====FILTER BY STATUS=====

	/**
	 * Find all Users by status active
	 * @param pageable : pagination paramters
	 * @return Page of active users.
	 */
	Page<User> findByIsActiveTrue(Pageable pageable);


	/**
	 * Find all Users with verified email
	 * @param pageable : pagination paramters
	 * @return Page of active users.
	 */
	Page<User> findByEmailVerifiedTrue(Pageable pageable);

}
