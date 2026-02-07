package com.conel.market.specifications;

import com.conel.market.models.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
    public static Specification<Product> priceLessThan(Double maxPrice){
        return (Root<Product> root,
                CriteriaQuery<?> query,
                CriteriaBuilder builder
        )->{
            if (maxPrice==null || maxPrice<=0)return null;
            return builder.lessThanOrEqualTo(root.get("price"),maxPrice);
        };
    }

    public static Specification<Product> nameLike(String name){
        return (Root<Product> root,
               CriteriaQuery<?> query,
               CriteriaBuilder builder
        )->{
           if (name==null || name.isEmpty()){
               return null;
           }
           return builder.like(builder.lower(root.get("name")),"%"+name.toLowerCase()+"%");
        };
    }

    public static Specification<Product> hasCategoryName(String categoryName){
        return (root, query, builder)->{
            if (categoryName==null || categoryName.isEmpty())return null;
            return builder.equal(root.join("category").get("name"),categoryName);
        };
    }
}