package com.fly.vimoo.shared.security;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * @author Halim
 * Configuration principale de Spring Security 6
 * <p>
 * Cette classe configure :
 * 1. Définit quelles URLs sont publiques/protégées
 * 2. Configure l'authentification JWT
 * 3. Gère les sessions (stateless avec JWT)
 * 4. Configure CORS pour le frontend React
 * 5. Définit comment chiffrer les mots de passe
 * 6. Installe notre filtre JWT personnalisé
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsService userDetailsService;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	/**
	 * Configuration principale de la chaîne de sécurité
	 * C'est ici que tout se configure : URLs, filtres, sessions, etc.
	 */

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {

		http
				//1. Desactivate CSRF (don't use with JWT)
				.csrf(AbstractHttpConfigurer::disable)

				//2. Configuration CORS
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				//3. Sessions : STATELESS, use JWT , not session
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				//4. Point d'entrée pour les erreurs d'authentification
				.exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

				.authorizeHttpRequests(auth -> auth
						// URLs publiques (pas d'authentification requise)
						.requestMatchers("/api/v1/auth/**").permitAll()
						.requestMatchers("/api/v1/public/**").permitAll()
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/swagger-ui/**", "/v1/api-docs/**").permitAll()
						.requestMatchers("/error").permitAll()

						// URLs protégées (authentification requise)
						.requestMatchers("/api/v1/users/me/**").authenticated()
						.requestMatchers("/api/v1/users/**").authenticated()
						.requestMatchers("/api/v1/watchlist/**").authenticated()
						.requestMatchers("/api/v1/reviews/**").authenticated()

						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

						.anyRequest().authenticated())

				//6. Add JWT filter before UsernamePasswordAuthenticationFilter standard
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	/***
	 * Utilisé pour valider les credentials lors du login.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}


	/**
	 * Provider d'authentification DAO
	 * Use for connect UserDetailsService + PasswordEncoder
	 */
	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {

		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration = new CorsConfiguration();

		//Origines autorisées (frontend)
		configuration.setAllowedOriginPatterns(List.of(
				"http://localhost:3000",    // React dev server
				"http://localhost:5173"  // Vite dev server
		));

		//Méthodes HTTP autorisées.
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

		//Headers autorisés
		configuration.setAllowedHeaders(List.of("*"));

		//Accepter envoi des credentials (cookies, auth headers)
		configuration.setAllowCredentials(true);

		//Cache preflight requests (use for optimization)
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;


	}


}

