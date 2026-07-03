package com.conel.market.repository.category;

import com.conel.market.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryRepository extends JpaRepository<Category,String> {
    boolean existsByNameIgnoreCase(String name);
    @Modifying
    @Transactional
    @Query(value = "UPDATE category SET deleted=false WHERE id = ?",nativeQuery = true)
    void restoreById(String id);
}
