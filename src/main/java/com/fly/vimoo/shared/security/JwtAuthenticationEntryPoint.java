package com.fly.vimoo.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * Point d'entrée pour les erreurs d'authentification
 * Cette classe est appelé quand :
 * 1. Un utilisateur non authentifié tente d'accéder à une URL protégée
 * 2. Un token JWT est invalide ou expiré.
 * 3. Aucun token n'est fourni pour la ressource protégée.
 * Return JSON standard.
 **/

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Methode appelé lors d'une erreur d'authentification
	 */

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

		//Log error.
		log.warn("Unauthorized access attempt: {} {} from IP: {}",
				request.getMethod(),
				request.getRequestURI(),
				getClientIpAddress(request));

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("timestamp", LocalDateTime.now().toString());
		errorResponse.put("status", 401);
		errorResponse.put("error", "Unauthorized");
		errorResponse.put("message", determineErrorMessage(request, authException));
		errorResponse.put("path", request.getRequestURI());

		objectMapper.writeValue(response.getOutputStream(), errorResponse);

	}


	private String determineErrorMessage(HttpServletRequest request, AuthenticationException authenticationException) {

		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && !authHeader.startsWith("Bearer ")) {
			return "Access token required. Please provide a valid JWT token in Authorization header.";
		}

		if (authenticationException.getMessage().contains("expired")) {
			return "Access token has expired. Please refresh your token.";
		}

		if (authenticationException.getMessage().contains("invalid")) {
			return "Invalid access token. Please login again.";
		}

		return "Authentication failed. Please provide a valid access token.";


	}


	/**
	 * Récupère l'IP du client
	 */
	private String getClientIpAddress(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0].trim();
		}

		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty()) {
			return xRealIp;
		}

		return request.getRemoteAddr();
	}
}
