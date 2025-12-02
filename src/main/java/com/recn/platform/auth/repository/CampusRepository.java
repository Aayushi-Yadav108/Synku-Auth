package com.recn.platform.auth.repository;

import com.recn.platform.auth.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, String> {

    Optional<Campus> findByUserId(String userId);

    boolean existsByUserId(String userId);
}

