package com.conel.market.specifications;

import com.conel.market.models.products.Product;
import jakarta.persistence.criteria.*;
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

            // 1. If Spring is just COUNTING rows (Long), do a lightweight JOIN.
            if (Long.class.equals(query.getResultType())) {
                return builder.equal(root.join("category").get("name"), categoryName);
            }

            // 2. If Spring is FETCHING data rows, turn it into a FETCH JOIN.
            // This grabs products AND categories in 1 single database hit! (Prevents N+1)
            Join<Object, Object> categoryJoin =
                    (Join<Object, Object>) root.fetch("category", JoinType.INNER);

            return builder.equal(categoryJoin.get("name"), categoryName);
        };
    }
}