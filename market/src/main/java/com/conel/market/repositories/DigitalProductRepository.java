package com.conel.market.repositories;

import com.conel.market.models.DigitalProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalProductRepository extends JpaRepository<DigitalProduct,Integer> {
}
