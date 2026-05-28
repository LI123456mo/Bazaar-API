package com.conel.market.models.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    List<Product> findAllByNameContainingIgnoreCase(String name);

    List<Product> findAllByNameIgnoreCaseStartingWith(String name);

    List<Product> findAllByNameIgnoreCaseEndsWith(String name);

    List<Product> findAllByNameInIgnoreCase(List<String> name);

    @Modifying
    @Query("""
            UPDATE Product p 
            SET p.active = false 
            WHERE p.category.id = :categoryId
           """)
    void archiveAllByCategoryId(@Param("categoryId") String categoryId);
}