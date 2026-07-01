
package com.conel.market.models.products;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    @EntityGraph(attributePaths = {"category", "seller"})
    Optional<Product> findById(String id);

    @EntityGraph(attributePaths = {"category", "seller"})
    List<Product> findAllByNameContainingIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Product p SET p.active = false WHERE p.category.id = :categoryId")
    void archiveAllByCategoryId(@Param("categoryId") String categoryId);
}