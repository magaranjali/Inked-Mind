package com.virinchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virinchi.model.UserClass;

@Repository
public interface UserRepository extends JpaRepository<UserClass, Integer>{
    
    boolean existsByEmailAndPassword(String email, String password);
    UserClass findByEmail(String email);
    UserClass findTopByEmailOrderByIdDesc(String email);
    long countByRoleIgnoreCase(String role);
    boolean existsByEmailIgnoreCase(String email);

}
