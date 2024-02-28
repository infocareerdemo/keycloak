package com.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.model.UserDetails;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long>{

	List<UserDetails> findByEmail(String email);

	List<UserDetails> findByUserName(String userName);

	List<UserDetails> findByUserNameAndIdNotIn(String userName, List<Long> list);

	List<UserDetails> findByEmailAndIdNotIn(String email, List<Long> list);
}
