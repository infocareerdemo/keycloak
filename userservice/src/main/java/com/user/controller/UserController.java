package com.user.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.dto.RoleDto;
import com.user.dto.UserDetailsDto;
import com.user.service.UserService;

import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserService userService;
	
	@PostMapping("/login")
	public ResponseEntity<Object> login(@RequestBody Map<String, String> login){
		return new ResponseEntity<Object>(userService.login(login),HttpStatus.OK);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Object> logout(@RequestBody Map<String, String> token){
		return new ResponseEntity<Object>(userService.logout(token),HttpStatus.OK);
	}
	
	@PostMapping("/saveRole")
	public ResponseEntity<Object> saveRole(@RequestBody RoleDto roleDto) {
		return new ResponseEntity<Object>(userService.saveRole(roleDto),HttpStatus.OK);
	}
	
	@PostMapping("/save")
	public ResponseEntity<Object> saveUser(@RequestBody UserDetailsDto userDetailsDto) {
		return new ResponseEntity<Object>(userService.saveUser(userDetailsDto),HttpStatus.OK);
	}
	
	@GetMapping("/all")
	@RolesAllowed("admin")
	public ResponseEntity<Object> getAllUser() {
		return new ResponseEntity<Object>(userService.getAllUser(),HttpStatus.OK);
	}
	
	@GetMapping
	public ResponseEntity<Object> getUser(Principal principal) {
		return new ResponseEntity<Object>(userService.getUser(principal.getName()),HttpStatus.OK);
	}
	
	@PostMapping("/update")
	public ResponseEntity<Object> updateUser(@RequestBody UserDetailsDto userDetailsDto) {
		return new ResponseEntity<Object>(userService.updateUser(userDetailsDto),HttpStatus.OK);
	}
	
	@GetMapping("/resetPassword")
	public ResponseEntity<Object> resetPassword(Principal principal, String password) {
		return new ResponseEntity<Object>(userService.resetPassword(principal.getName(), password),HttpStatus.OK);
	}
}
