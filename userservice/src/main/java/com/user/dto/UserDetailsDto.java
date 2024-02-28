package com.user.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserDetailsDto {

	private long id;
	private String userName;
	private String email;
	private String firstName;
	private String lastName;
	private String password;
	private long mobile;
	private String city;
	private long roleId;
	private RoleDto roleDto;
	private LocalDateTime createdDate;
}
