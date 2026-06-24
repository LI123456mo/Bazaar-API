package com.conel.market.models.products;

import com.conel.market.models.category.Category;
import com.conel.market.models.products.dto.ProductRequest;
import com.conel.market.models.products.dto.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    // Converts incoming Request payloads into database Entities
    public Product toProduct(ProductRequest dto) {
        if (dto == null) {
            return null;
        }

        var product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStockQuantity(dto.stockQuantity());

        if (dto.categoryId() != null) {
            var category = new Category();
            category.setId(dto.categoryId());
            product.setCategory(category);
        }

        return product;
    }

    public ProductResponse toProductResponseDto(Product product) {
        if (product == null) {
            return null;
        }

        String categoryId = null;
        String categoryName = null;

        if (product.getCategory() != null) {
            categoryId = product.getCategory().getId();
            categoryName = product.getCategory().getName();
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                categoryName,
                product.getCreatedBy() != null ? product.getCreatedBy() : "Marketplace Merchant"
        );
    }
}