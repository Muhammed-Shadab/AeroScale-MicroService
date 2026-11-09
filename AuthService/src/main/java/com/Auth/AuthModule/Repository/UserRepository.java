package com.Auth.AuthModule.Repository;

import com.Auth.AuthModule.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByEmailIgnoreCase(String email);



    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhoneNo(String phoneNo);
}
