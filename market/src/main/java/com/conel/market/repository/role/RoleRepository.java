package com.conel.market.repository.role;

import com.conel.market.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,String> {
    Optional<Role> findByName(String roleUser);


    // JPA auto-implemented method to check existence by name (idempotent seeding)
    boolean existsByName(String name);
}
