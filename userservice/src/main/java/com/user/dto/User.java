package com.user.dto;

import lombok.Data;

@Data
public class User {

	private String username; 
	private String email;
	private String firstName;
	private String lastName;
	private String password;
	private int roleId;
}
