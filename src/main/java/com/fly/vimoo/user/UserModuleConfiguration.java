package com.fly.vimoo.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Halim
 * Configuration principale du module User
 *
 * Cette classe centralise la configuration du module User et définit :
 * 1. Le scan des composants Spring (@Service, @Controller,...)
 * 2. Le scan des entités JPA (User, UserSession, etc.)
 * 3. Le scan des repositories Spring Data
 *
 * * Pourquoi cette approche ?
 *  * - Isolation des modules : chaque module gère sa propre configuration
 *  * - Facilite la migration vers microservices plus tard
 *  * - Configuration explicite et claire
 *  * - Permet des tests unitaires par module
 *  */




@Configuration
@ComponentScan(basePackages = {
		"com.fly.vimoo.user.api", //Controller REST
		"com.fly.vimoo.user.application", // Application Services
		"com.fly.vimoo.user.domain", // Domain Services
		"com.fly.vimoo.user.infrastructure" // Repositories and adapters.
})
@EntityScan(basePackages = {
		"com.fly.vimoo.user.domain.entity"  // JPA Entities (User, UserSession, etc.)
})
@EnableJpaRepositories(basePackages = {
		"com.fly.vimoo.user.infrastructure.repository" // JPA Entities (User, UserSession, etc.)
}
)
@Slf4j
public class UserModuleConfiguration {

	public UserModuleConfiguration() {
		log.info("Initializing UserModuleConfiguration");
	}
}
