package com.user.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;

@Configuration
public class BeanConfig {

	@Value("${spring.security.oauth2.client.registration.spring_template.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.spring_template.client-secret}")
	private String clientSecret;

	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;

	@Value("${spring.security.oauth2.client.registration.spring_template.provider}")
	private String realm;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
//	@Bean
//    public Jersey3TransportClientFactories jersey3TransportClientFactories() {
//        return new Jersey3TransportClientFactories();
//    }

	@Bean
	public Keycloak keycloak() {
		return KeycloakBuilder.builder().serverUrl(authServerUrl).realm(realm)
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS).clientId(clientId).clientSecret(clientSecret).build();
	}
}
