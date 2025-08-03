package com.fly.vimoo.shared.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de gestion des tokens JWT : A ce niveau , la connexion est OK
 * <p>
 * 1. Créer des access tokens
 * 2. Créer des refresh tokens
 * 3. Valider les tokens reçus
 * 4. Extraire informations des tokens.
 * 5. Vérifier expiration des tokens.
 */


@Component
@Slf4j
public class JwtTokenProvider {

	@Value("${security.jwt.secret}")
	private String jwtSecret;

	@Value("${security.jwt.access-token-expiration}")
	private String accessTokenExpiration; //15 min in ms

	@Value("${security.jwt.refresh-token-expiration}")
	private String refreshTokenExpiration; //7 Jours in ms


	/**
	 * Validation du secret JWT au démarrage.
	 */
	@PostConstruct
	public void validateJwtSecret() {
		if (jwtSecret.length() < 64) {
			throw new IllegalStateException(
					"JWT secret must be at least 64 characters long. " +
							"Current : " + jwtSecret.length() + " characters");
		}

		if ("please-change-this-secret-in-production".equals(jwtSecret)) {
			log.warn("Using default JWT secret! Change it immediately!");
		}

		log.info("JWT Token Provider initialized successfully");

	}


	/**
	 * Génère la clé de signature à partir du secret.
	 */
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}


	/**
	 * Génère un access token depuis une récupération d'authentification
	 * Utilisé lors du login.
	 *
	 * @param authentication
	 */
	public String generateAccessToken(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		return generateToken(userPrincipal.getUsername(), Long.parseLong(accessTokenExpiration), "access");

	}


	/**
	 * Méthode privée pour générer un token
	 * <p>
	 * Structure du token JWT :
	 * Header: { "alg": "HS256", "typ": "JWT" }
	 * Payload: { "sub": "username", "type": "access", "iat": 1234567890, "exp": 1234567890 }
	 * Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
	 */
	private String generateToken(String username, long expiration, String tokenType) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		// Claims personnalisés
		Map<String, Object> claims = new HashMap<>();
		claims.put("type", tokenType);           // "access" ou "refresh"
		claims.put("iat", now.getTime() / 1000); // Issued At (timestamp)

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)                    // Username de l'utilisateur
				.setIssuedAt(now)                       // Date de création
				.setExpiration(expiryDate)              // Date d'expiration
				.signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signature
				.compact();
	}

	/**
	 * Extrait le username depuis un token.
	 */
	public String getUsernameFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getSubject();
	}

	/**
	 * Extrait le type de token (access/refresh)
	 */
	public String getTokenType(String token) {
		Claims claims = Jwts.parser()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.get("type", String.class);
	}


	/**
	 * Valide un token JWT.
	 * Vérifie :
	 * 1. La signature (token non modifié).
	 * 2. La date d'expiration.
	 * 3. Le format
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.setSigningKey(getSigningKey())
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (SecurityException ex) {
			log.error("Invalid JWT signature {}", ex.getMessage());
		} catch (MalformedJwtException ex) {
			log.error("Invalid JWT token {}", ex.getMessage());
		} catch (ExpiredJwtException ex) {
			log.error("Expired JWT token {}", ex.getMessage());
		} catch (UnsupportedJwtException ex) {
			log.error("Unsupported JWT token {}", ex.getMessage());
		} catch (IllegalArgumentException ex) {
			log.error("JWT claims string is empty {}", ex.getMessage());
		}
		return false;
	}

	public boolean isAccessToken(String token) {
		try {
			return "access".equals(getTokenType(token));
		} catch (Exception e) {
			return false;
		}
	}


	public boolean isRefreshToken(String token) {
		try {
			return "refresh".equals(getTokenType(token));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Extrait la date d'expiration d'un token
	 */
	public Date getExpirationDateFromToken(String token) {
		Claims claims = Jwts.parser()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getExpiration();
	}
}
