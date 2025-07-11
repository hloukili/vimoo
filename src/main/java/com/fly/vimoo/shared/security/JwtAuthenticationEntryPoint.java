package com.fly.vimoo.shared.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * Point d'entrée pour les erreurs d'authentification
 * Cette classe est appelé quand :
 * 1. Un utilisateur non authentifié tente d'accéder à une URL protégée
 * 2. Un token JWT est invalide ou expiré.
 * 3. Aucun token n'est fourni pour la ressource protégée.
 * Return JSON standard.
 *
 **/

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

	}
}
