package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{

}
