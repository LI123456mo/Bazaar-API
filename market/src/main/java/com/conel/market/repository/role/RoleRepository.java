package com.conel.market.repository.role;

import com.conel.market.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,String> {
    Optional<Role> findByName(String roleUser);
}
