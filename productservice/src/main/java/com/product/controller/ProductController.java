package com.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/product")
public class ProductController {

	@GetMapping("/admin")
	@RolesAllowed("admin")
	public ResponseEntity<Object> getProductForAdmin() {
		return new ResponseEntity<Object>("This Is Admin Only Access", HttpStatus.OK);
	}
	
	@GetMapping("/user")
	@RolesAllowed("user")
	public ResponseEntity<Object> getProductForUser() {
		return new ResponseEntity<Object>("This is User Only Access", HttpStatus.OK);
	}
}
