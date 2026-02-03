package com.conel.market.repositories;

import com.conel.market.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> , JpaSpecificationExecutor<Product> {

    List<Product> findAllByNameContainingIgnoreCase(String name);
    List<Product> findAllByNameIgnoreCaseStartingWith(String name);
    List<Product> findAllByNameIgnoreCaseEndsWith(String name);
    List<Product> findAllByNameInIgnoreCase(List<String> name);
}
