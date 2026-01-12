package com.sampath.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sampath.portal.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
   
    // Case-insensitive lookup for Oracle
    Optional<User> findByUsernameIgnoreCase(String username);

}
