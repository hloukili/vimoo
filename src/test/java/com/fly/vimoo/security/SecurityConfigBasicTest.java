package com.fly.vimoo.security;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityConfigBasicTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	//TODO: Add Custom Error for 404 not found.
	@Test
	void whenAccessEndpointNonExistent_thenNotFound() throws Exception {
		mockMvc.perform(get("/api/notfound"))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void whenAccessHealthEndpoint_thenOk() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void whenMakeCorsPreflightRequest_thenCorsHeadersReturned() throws Exception {
		mockMvc.perform(options("/api/v1/auth/login")
						.header("Origin", "http://localhost:3000")
						.header("Access-Control-Request-Headers", "Content-Type")
						.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
				.andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
				.andExpect(header().exists("Access-Control-Allow-Headers"));


	}

	@Test
	void whenAccessProtectedPath_withoutToken_thenUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/users/me"))
				.andDo(print())

				.andExpect(status().isUnauthorized());
	}

	@Test
	void whenAccessAdminPath_withoutToken_thenUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/admin/123"))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}


	@Test
	void whenAccessProtectedPath_withMalFormedToken_thenUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/users/me")
						.header("Authorization", "Bearer invalid token"))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}


	@Test
	void whenAccessProtectedPath_withoutBearerPrefix_thenUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/users/me")
						.header("Authorization", "invalid token"))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}


}

