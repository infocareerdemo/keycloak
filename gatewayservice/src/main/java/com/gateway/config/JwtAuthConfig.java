package com.gateway.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthConfig implements Converter<Jwt, Mono<AbstractAuthenticationToken>>{

	@Value("${spring.security.oauth2.client.registration.gateway.client-id}")
	private String clientId;
	
	@Override
	public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
		return Mono.just(extractResourceRoles(jwt)).map(auth -> new JwtAuthenticationToken(jwt, auth,getPrincipalClaimName(jwt)));
	}

	private String getPrincipalClaimName(Jwt jwt) {
		String claimName = JwtClaimNames.SUB;
		return jwt.getClaim(claimName);
	}

	private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {

//		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

		Collection<String> allRoles = new ArrayList<>();
		Collection<String> resourceRoles;
//		Collection<String> realmRoles;

		// For Client Roles
		if (resourceAccess != null && resourceAccess.get(clientId) != null) {
			Map<String, Object> account = (Map<String, Object>) resourceAccess.get(clientId);
			if (account.containsKey("roles")) {
				resourceRoles = (Collection<String>) account.get("roles");
				allRoles.addAll(resourceRoles);
			}
		}
		// For Realm Roles
//		if (realmAccess != null && realmAccess.containsKey("roles")) {
//			realmRoles = (Collection<String>) realmAccess.get("roles");
//			allRoles.addAll(realmRoles);
//		}
		return allRoles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toSet());
	}
}
