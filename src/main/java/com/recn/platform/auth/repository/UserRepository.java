package com.recn.platform.auth.repository;

import com.recn.platform.auth.entity.User;
import com.recn.platform.auth.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByUserType(UserType userType);

    List<User> findByIsActive(Boolean isActive);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.isVerified = true")
    List<User> findAllActiveAndVerified();

    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil < :now")
    List<User> findExpiredLockedAccounts(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.isActive = true")
    Long countActiveUsersByType(@Param("userType") UserType userType);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);
}

