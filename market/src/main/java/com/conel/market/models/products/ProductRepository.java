
package com.conel.market.models.products;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"category", "seller"})
    Optional<Product> findById(String id);

    @EntityGraph(attributePaths = {"category", "seller"})
    List<Product> findAllByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"category", "seller"})
    Page<Product> findBySellerId(String sellerId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") String id);

    @Modifying
    @Query("UPDATE Product p SET p.active = false WHERE p.category.id = :categoryId")
    void archiveAllByCategoryId(@Param("categoryId") String categoryId);
}