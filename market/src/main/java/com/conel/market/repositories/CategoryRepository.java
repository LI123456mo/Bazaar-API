package com.conel.market.repositories;

import com.conel.market.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryRepository extends JpaRepository<Category,Integer>, JpaSpecificationExecutor<Category> {
    boolean existsByName(String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE category SET deleted=false WHERE id = ?",nativeQuery = true)
    void restoreById(Integer id);
}
