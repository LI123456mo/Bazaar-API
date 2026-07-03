package com.conel.market.user.repository;

import com.conel.market.user.entity.User;
import com.conel.market.entity.vendor.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {

    @EntityGraph(attributePaths = "roles")// Load roles in one query
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    //ADMINISTRATIVE OVERRIDE: Bypasses @SQLRestriction to find deleted profiles
    @Query(value = "SELECT * FROM _user u WHERE LOWER(u.email) = LOWER(:email)", nativeQuery = true)
    Optional<User> findAnyUserByEmailIncludeDeleted(@Param("email") String email);

    @EntityGraph(attributePaths = "roles")
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE r.name = 'VENDOR'
            AND (:status IS NULL OR u.vendorStatus = :status)
            ORDER BY u.vendorStatus ASC
            """)
    Page<User> findAllVendors(
            @Param("status") VendorStatus status,
            Pageable pageable
    );
}
