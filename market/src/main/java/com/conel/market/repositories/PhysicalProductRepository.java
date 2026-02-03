package com.conel.market.repositories;

import com.conel.market.models.PhysicalProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhysicalProductRepository extends JpaRepository<PhysicalProduct,Integer> {
}
