package com.recn.platform.auth.repository;

import com.recn.platform.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByPermissionName(String permissionName);

    boolean existsByPermissionName(String permissionName);

    List<Permission> findByResource(String resource);

    List<Permission> findByResourceAndAction(String resource, String action);
}

