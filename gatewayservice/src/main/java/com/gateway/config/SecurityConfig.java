package com.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

	@Autowired
	JwtAuthConfig jwtAuthConfig;

	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConfig)))
				.authorizeExchange(req -> req.pathMatchers("/user/login", "/user/logout", "/user/save", "/user", "/user/update", "/user/resetPassword")
						.permitAll().pathMatchers("/user/saveRole", "/user/all", "/product/admin").hasRole("admin")
						.pathMatchers("/product/user").hasRole("user").anyExchange().authenticated());
		return http.build();
	}
}
