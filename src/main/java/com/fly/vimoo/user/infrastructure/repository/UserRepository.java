package com.fly.vimoo.user.infrastructure.repository;

import com.fly.vimoo.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
	 *
	 * @param token Verification Token
	 * @param now   current date
	 **/
	@Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.emailVerificationExpires > :now")
	Optional<User> findByValidEmailVerificationToken(@Param("token") String token,
													 @Param("now") LocalDateTime now);


	/**
	 * Find User by Token reset password valid (no expires)
	 *
	 * @param token Verification Token
	 * @param now   current date
	 **/
	@Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpires > :now")
	Optional<User> findByValidPasswordResetToken(@Param("token") String token,
												 @Param("now") LocalDateTime now);


	// =====FILTER BY STATUS=====

	/**
	 * Find all Users by status active
	 *
	 * @param pageable : pagination parameters
	 * @return Page of active users.
	 */
	Page<User> findByIsActiveTrue(Pageable pageable);


	/**
	 * Find all Users with verified email
	 *
	 * @param pageable : pagination parameters
	 * @return Page of active users.
	 */
	Page<User> findByEmailVerifiedTrue(Pageable pageable);


	/**
	 * Find all Users with verified email and active
	 *
	 * @param pageable : pagination parameters
	 * @return Page of active users and verified email.
	 */
	Page<User> findByIsActiveTrueAndEmailVerifiedTrue(Pageable pageable);


	/**
	 * Find all Users with public profile
	 *
	 * @param pageable : pagination parameters
	 * @return Page of users with public profile.
	 */
	Page<User> findByIsPublicTrue(Pageable pageable);


	// =====SEARCH AND FILTER=====

	/**
	 * Find users by username
	 *
	 * @param pageable : pagination parameters
	 * @return Page of users.
	 */
	@Query("SELECT u FROM User u  WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))" +
			"AND u.isActive = true AND u.emailVerified = true")
	Page<User> searchByUsername(@Param("username") String username, Pageable pageable);


	/**
	 * Find users by full name
	 *
	 * @param searchTerm : search term.
	 * @param pageable   : pagination parameters
	 * @return Page of users.
	 */
	@Query("SELECT u FROM User u WHERE " +
			"(LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
			"OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
			"OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
			"AND u.isActive = true AND u.emailVerified = true")
	Page<User> searchByFullName(@Param("searchTerm") String searchTerm, Pageable pageable);


	//TODO : make query
	Page<User> globalSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

	// ====ROLE GESTION===


	/**
	 * Find all users by role.
	 *
	 * @param role     role to find
	 * @param pageable Paramètres de pagination
	 * @return Users page with specified role
	 */
	Page<User> findByRole(User.Role role, Pageable pageable);


	/**
	 * Find all active admins
	 *
	 * @return List of active admins
	 */
	@Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
	List<User> findActiveAdmins();


	// === COUNTERS ===

	/**
	 * Count total number of active users
	 *
	 * @return Number of active users.
	 */
	@Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
	long countActiveUsers();

	/**
	 * Count all users with verified email
	 *
	 * @return Number of verified users
	 */
	@Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
	long countVerifiedUsers();


	/**
	 * Count users created after a specific date.
	 *
	 * @param date Date to compare
	 * @return Number of users created after this date.
	 */
	@Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :date")
	long countUsersCreatedAfter(@Param("date") LocalDateTime date);


	// === UPDATE ===


	/**
	 * Update last login date of user.
	 *
	 * @param userId    ID user
	 * @param lastLogin Date last connexion
	 * @return Number of rows updated
	 */
	@Modifying
	@Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
	int updateLastLogin(@Param("userId") UUID userId, @Param("lastLogin") LocalDateTime lastLogin);


	/**
	 * Mark user like verified.
	 *
	 * @param userId ID user
	 * @return Number of rows updated
	 */
	@Modifying
	@Query("UPDATE User u SET u.emailVerified = true, " +
			"u.emailVerificationToken = null, " +
			"u.emailVerificationExpires = null" +
			" WHERE u.id = :userId")
	int markEmailAsVerified(@Param("userId") UUID userId);



	/**
	 * Clean up expired email verification tokens
	 *
	 * @param now Date/time actual
	 * @return Number of tokens cleaned
	 */
	@Modifying
	@Query("UPDATE User u SET u.emailVerificationToken = null, u.emailVerificationExpires = null " +
			"WHERE u.emailVerificationExpires < :now")
	int cleanUpExpiredEmailVerificationTokens(@Param("now") LocalDateTime now);



	/**
	 * Clean up expired password reset tokens
	 *
	 * @param now Date/time actual
	 * @return Number of tokens cleaned
	 */
	@Modifying
	@Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetExpires = null " +
			"WHERE u.passwordResetExpires < :now")
	int cleanUpExpiredPasswordResetTokens(@Param("now") LocalDateTime now);



	// === SPECIAL REQUEST ===
	/**
	 * Find recent users
	 *
	 * @param daysAgo Number of days in past
	 * @param pageable Paramètres de pagination
	 * @return Page des nouveaux utilisateurs
	 * */
	@Query("SELECT u FROM User u WHERE u.createdAt > :daysAgo"
			+ " AND u.isActive = true ORDER BY u.createdAt DESC")
	Page<User> findRecentUsers(@Param("daysAgo") LocalDateTime daysAgo, Pageable pageable);


	/**
	 * Find users who never logged in
	 *
	 * @param pageable Paramètres de pagination
	 * @return Page des utilisateurs jamais connectés
	 */
	@Query("SELECT u FROM User u WHERE u.lastLogin IS NULL AND u.emailVerified = true")
	Page<User> findUsersNeverLoggedIn(Pageable pageable);




}
