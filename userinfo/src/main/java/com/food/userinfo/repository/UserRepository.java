package com.food.userinfo.repository;

import com.food.userinfo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);           // ← Add this

    Optional<User> findByAlternateMobileNumber(String newAltMobile);
}
