package com.user.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.user.dto.LoginResponse;
import com.user.dto.RoleDto;
import com.user.dto.User;
import com.user.dto.UserDetailsDto;
import com.user.model.Role;
import com.user.model.UserDetails;
import com.user.repository.RoleRepository;
import com.user.repository.UserDetailsRepository;

import jakarta.ws.rs.core.Response;

@Service
public class UserService {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	Keycloak keycloak;

	@Autowired
	UserDetailsRepository userDetailsRepository;

	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	EmailService emailService;

	@Value("${spring.security.oauth2.client.registration.spring_template.provider}")
	private String realm;
	@Value("${spring.security.oauth2.client.provider.spring_template.issuer-uri}")
	private String issuerUrl;
	@Value("${spring.security.oauth2.client.registration.spring_template.client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.client.registration.spring_template.client-secret}")
	private String clientSecret;
	@Value("${spring.security.oauth2.client.registration.spring_template.authorization-grant-type}")
	private String grantType;
	
	private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}|;:'\",.<>?";

	public LoginResponse login(Map<String, String> request) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
		data.add("client_id", clientId);
		data.add("client_secret", clientSecret);
		data.add("grant_type", grantType);
		data.add("username", request.get("username"));
		data.add("password", request.get("password"));
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(data,
				httpHeaders);
		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
				"http://localhost:8080/realms/spring_template/protocol/openid-connect/token", httpEntity,
				LoginResponse.class);
		return response.getBody();
	}

	public String logout(Map<String, String> token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
		data.add("client_id", clientId);
		data.add("client_secret", clientSecret);
		data.add("refresh_token", token.get("token"));
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(data,
				headers);
		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:8080/realms/spring_template/protocol/openid-connect/logout", httpEntity,
				String.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			return "Logged Out...!!!";
		}
		return response.getBody();
	}

	public User createUser(User user) {
		RealmResource realmResource = keycloak.realm(realm);
		UsersResource usersRessource = realmResource.users();

		UserRepresentation userRep = new UserRepresentation();
		userRep.setEnabled(true);
		userRep.setUsername(user.getUsername());
		userRep.setEmail(user.getEmail());
		userRep.setFirstName(user.getFirstName());
		userRep.setLastName(user.getLastName());
		userRep.setEmailVerified(false);

		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setValue(user.getPassword());
		credentialRepresentation.setTemporary(false);
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

		List<CredentialRepresentation> list = new ArrayList<>();
		list.add(credentialRepresentation);
		userRep.setCredentials(list);

		UsersResource usersResource = getUsersResource();
		Response response = usersResource.create(userRep);

		if (Objects.equals(201, response.getStatus())) {
			String userId = CreatedResponseUtil.getCreatedId(response);
			UserResource userResource = usersRessource.get(userId);
			// Get client
			ClientRepresentation app1Client = realmResource.clients().findByClientId(clientId).get(0);
			RoleRepresentation userClientRole = null;
			if (user.getRoleId() == 1) {
				// Get client level role (requires view-clients role)
				userClientRole = realmResource.clients().get(app1Client.getId()).roles().get("user").toRepresentation();
				userClientRole.setClientRole(true);
			} else if (user.getRoleId() == 2) {
				userClientRole = realmResource.clients().get(app1Client.getId()).roles().get("admin")
						.toRepresentation();
				userClientRole.setClientRole(true);
			} else if (user.getRoleId() == 3) {
				userClientRole = realmResource.clients().get(app1Client.getId()).roles().get("head").toRepresentation();
				userClientRole.setClientRole(true);
			}

			// Assign client level role to user
			userResource.roles() //
					.clientLevel(app1Client.getId()).add(Arrays.asList(userClientRole));

			// Assign realm level role to user
//			userResource.roles().realmLevel().add(Arrays.asList(userClientRole));
			return user;
		}
		return null;
	}
	
	public static String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            password.append(randomChar);
        }

        return password.toString();
    }

	private UsersResource getUsersResource() {
		RealmResource realm1 = keycloak.realm(realm);
		return realm1.users();
	}

	public String saveRole(RoleDto roleDto) {
		Role role = new Role();
		role.setRole(roleDto.getRole());
		roleRepository.save(role);
		return "Role Saved...!";
	}

	public LinkedHashMap<String, Object> saveUser(UserDetailsDto userDetailsDto) {
		LinkedHashMap<String, Object> entity = new LinkedHashMap<>();
		List<UserDetails> userEmail = userDetailsRepository.findByEmail(userDetailsDto.getEmail());
		List<UserDetails> userName = userDetailsRepository.findByUserName(userDetailsDto.getUserName());

		// check if email already exists in keycloak
		List<UserRepresentation> emailKeycloak = keycloak.realm(realm).users().searchByEmail(userDetailsDto.getEmail(),
				true);

		// check if username already exists in keycloak
		List<UserRepresentation> userKeycloak = keycloak.realm(realm).users()
				.searchByUsername(userDetailsDto.getUserName(), true);

		if (!CollectionUtils.isEmpty(userEmail) || !CollectionUtils.isEmpty(emailKeycloak)) {
			entity.put("status", 409);
			entity.put("message", "User Already Exists With Same Email");
			return entity;
		}
		if (!CollectionUtils.isEmpty(userName) || !CollectionUtils.isEmpty(userKeycloak)) {
			entity.put("status", 409);
			entity.put("message", "User Already Exists With Same UserName");
			return entity;
		}
		UserDetails userDetails = new UserDetails();
		BeanUtils.copyProperties(userDetailsDto, userDetails);
		userDetails.setCreatedDate(LocalDateTime.now());
		User user = new User();
		user.setEmail(userDetailsDto.getEmail());
		user.setFirstName(userDetailsDto.getFirstName());
		user.setLastName(userDetailsDto.getLastName());
		user.setPassword(userDetailsDto.getPassword());
		user.setUsername(userDetailsDto.getUserName());
		user.setRoleId((int) userDetailsDto.getRoleId());
		String password = generateRandomPassword(16);
		user.setPassword(password);
		userDetailsDto.setPassword(password);
		emailService.sendPasswordForNewUser(userDetailsDto);
		User userRes = createUser(user);
		if (userRes == null) {
			entity.put("status", 409);
			entity.put("message", "Error Occured While Saving In KeyCloak...!");
			return entity;
		}
		userDetails.setPassword(password);
		userDetailsRepository.save(userDetails);
		entity.put("status", 200);
		entity.put("message", "UserDetails Saved...!");
		entity.put("data", userDetails);
		return entity;
	}

	public List<UserDetailsDto> getAllUser() {
		List<UserDetails> userDetails = userDetailsRepository.findAll();
		List<UserDetailsDto> userDetailsResponse = new ArrayList<>();
		if (!CollectionUtils.isEmpty(userDetails)) {
			for (UserDetails userDetail : userDetails) {
				UserDetailsDto userDetailsDto = new UserDetailsDto();
				BeanUtils.copyProperties(userDetail, userDetailsDto);
				Optional<Role> role = roleRepository.findById(userDetail.getRoleId());
				if (role.isPresent()) {
					RoleDto roleDto = new RoleDto();
					BeanUtils.copyProperties(role, roleDto);
					userDetailsDto.setRoleDto(roleDto);
				}
				userDetailsResponse.add(userDetailsDto);
			}
		}
		return userDetailsResponse;
	}

	public UserDetailsDto getUser(String userName) {
		UsersResource usersResource = getUsersResource();
		UserResource userResource = usersResource.get(userName);
		List<UserDetails> userDetails = userDetailsRepository
				.findByUserName(userResource.toRepresentation().getUsername());
		UserDetailsDto userDetailsDto = new UserDetailsDto();
		if (!CollectionUtils.isEmpty(userDetails)) {
			UserDetails userDetail = userDetails.get(0);
			BeanUtils.copyProperties(userDetail, userDetailsDto);
			Optional<Role> role = roleRepository.findById(userDetail.getRoleId());
			if (role.isPresent()) {
				RoleDto roleDto = new RoleDto();
				roleDto.setId(role.get().getId());
				roleDto.setRole(role.get().getRole());
				userDetailsDto.setRoleDto(roleDto);
			}
		}
		return userDetailsDto;
	}

	public UserDetailsDto updateUserInKeyCloak(UserDetailsDto userDetailsDto, String userName) {
		UsersResource usersResource = getUsersResource();
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername(userDetailsDto.getUserName());
		userRepresentation.setEmail(userDetailsDto.getEmail());
		userRepresentation.setFirstName(userDetailsDto.getFirstName());
		userRepresentation.setLastName(userDetailsDto.getLastName());
		userRepresentation.setEnabled(true);
		userRepresentation.setEmailVerified(false);
		UserRepresentation user = usersResource.search(userName).stream().findFirst().orElse(null);
		usersResource.get(user.getId()).update(userRepresentation);
		return userDetailsDto;
	}

	public LinkedHashMap<String, Object> updateUser(UserDetailsDto userDetailsDto) {
		LinkedHashMap<String, Object> entity = new LinkedHashMap<>();
		Optional<UserDetails> userDetails = userDetailsRepository.findById(userDetailsDto.getId());
		UserDetails userDetail = null;
		if (userDetails.isPresent()) {
			if (!userDetails.get().getUserName().equals(userDetailsDto.getUserName())) {
				List<UserRepresentation> userKeycloak = keycloak.realm(realm).users()
						.searchByUsername(userDetailsDto.getUserName(), true);
				if (!CollectionUtils.isEmpty(userKeycloak)) {
					entity.put("status", 409);
					entity.put("message", "User Already Exists With Same UserName");
					return entity;
				}
			} else if (!userDetails.get().getEmail().equals(userDetailsDto.getEmail())) {
				List<UserRepresentation> emailKeycloak = keycloak.realm(realm).users()
						.searchByEmail(userDetailsDto.getEmail(), true);
				if (!CollectionUtils.isEmpty(emailKeycloak)) {
					entity.put("status", 409);
					entity.put("message", "User Already Exists With Same Email");
					return entity;
				}
			}
			List<UserDetails> userNameList = userDetailsRepository
					.findByUserNameAndIdNotIn(userDetailsDto.getUserName(), Arrays.asList(userDetailsDto.getId()));
			List<UserDetails> userEmailList = userDetailsRepository.findByEmailAndIdNotIn(userDetailsDto.getUserName(),
					Arrays.asList(userDetailsDto.getId()));
			if (CollectionUtils.isEmpty(userNameList)) {
				if (CollectionUtils.isEmpty(userEmailList)) {
					userDetail = userDetails.get();
					String userName =userDetail.getUserName();
					userDetailsDto.setCreatedDate(userDetail.getCreatedDate());
					BeanUtils.copyProperties(userDetailsDto, userDetail);
					// Update User in Keycloak
					updateUserInKeyCloak(userDetailsDto, userName);
					userDetailsRepository.save(userDetail);

					entity.put("status", 200);
					entity.put("message", "UserDetails Saved...!");
					entity.put("data", userDetail);
					return entity;
				} else {
					entity.put("status", 409);
					entity.put("message", "User Already Exists With Same Email");
					return entity;
				}
			} else {
				entity.put("status", 409);
				entity.put("message", "User Already Exists With Same UserName");
				return entity;
			}
		}
		return entity;
	}
	
	public UserDetailsDto resetPassword(String userName, String password) {
		UsersResource usersResource = getUsersResource();
		UserResource userResource = usersResource.get(userName);
		List<UserDetails> userDetails = userDetailsRepository.findByUserName(userResource.toRepresentation().getUsername());
		if (!CollectionUtils.isEmpty(userDetails)) {
			Optional<UserDetails> userDetail = userDetails.stream().findFirst();
			if (userDetail.isPresent()) {
				userDetail.get().setPassword(password);
				// reset password in keycloak
				CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
				credentialRepresentation.setValue(password);
				credentialRepresentation.setTemporary(false);
				credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
				userResource.resetPassword(credentialRepresentation);
				UserDetailsDto userDetailsDto = new UserDetailsDto();
				BeanUtils.copyProperties(userDetail.get(), userDetailsDto);
				return userDetailsDto;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
