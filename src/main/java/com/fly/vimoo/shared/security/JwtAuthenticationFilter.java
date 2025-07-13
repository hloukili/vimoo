package com.fly.vimoo.shared.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT
 * <p>
 * Ce filtre s'exécute sur chaque requête HTTP et :
 * 1. Extrait le token JWT du header Authorization
 * 2. Valide le token JWT
 * 3. Charge l'utilisateur depuis la DB
 * 4. Configure le SecurityContexte pour Spring Security.
 */


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsService userDetailsService;

	/**
	 * Methode principale du filtre, s'éxécute une fois par requête
	 */

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


		try {
			//1. Extraire le token JWT de la requête
			String token = getTokenFromRequest(request);

			// 2. Si token présent et valide
			if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

				//3. Vérifier que c'est bien un access token (not refresh)
				if (jwtTokenProvider.isAccessToken(token)) {

					//4. Extraire le username du token
					String username = jwtTokenProvider.getUsernameFromToken(token);

					//5. Charger l'utilisateur depuis la DB
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);

					//6. If user found, configure authentication
					if (userDetails != null) {
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								userDetails,
								null,
								userDetails.getAuthorities()); // Rôles, permissions
						//Ajouter des détails de la requête (IP, session,...)
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						//7. Définir l'authentication dans le SecurityContext
						// Maintenant Spring Security sait que l'utilisateur est authentifié
						SecurityContextHolder.getContext().setAuthentication(authentication);

						log.debug("User {} is authenticated successfully", username);
					}
				} else {
					// Tentative d'utiliser un refresh token pour l'authentification
					log.warn("Refresh token used for authentication attempt from IP: {}",
							getClientIpAddress(request));
				}

			} else {
				log.warn("No JWT token found or invalid in request from IP: {}", getClientIpAddress(request));
			}

		} catch (Exception e) {
			log.error("Cannot set user authentication in security context, message : {}", e.getMessage());
		}
		//8. Passer la requête au filtre suivant
		//Si pas d'authentification, Spring va retourner une 401
		filterChain.doFilter(request, response);
	}


	/**
	 * Extrait le token JWT depuis le header Authorization
	 * <p>
	 * Header attendu : "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
	 */
	private String getTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7); // Remove "Bearer " prefix
		}
		return null;
	}

	/**
	 * Récupère l'adresse IP réelle du client
	 * Gère les proxy/load balancers
	 */
	private String getClientIpAddress(HttpServletRequest request) {
		// Header X-Forwarded-For (proxy/load balancer)
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(xForwardedFor)) {
			return xForwardedFor.split(",")[0].trim();
		}

		// Header X-Real-IP (nginx)
		String xRealIp = request.getHeader("X-Real-IP");
		if (StringUtils.hasText(xRealIp)) {
			return xRealIp;
		}

		// IP directe
		return request.getRemoteAddr();
	}


	/**
	 * Ne pas appliquer le filtre sur certaines URLs
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();

		// Ne pas filtrer les assets statiques
		return path.startsWith("/static/") ||
				path.startsWith("/public/") ||
				path.equals("/favicon.ico");
	}

}
