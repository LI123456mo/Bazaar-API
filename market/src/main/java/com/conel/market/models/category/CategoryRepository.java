package com.conel.market.models.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,String> {
    boolean existsByNameIgnoreCase(String name);
}
