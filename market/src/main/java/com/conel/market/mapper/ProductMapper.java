package com.conel.market.mapper;

import com.conel.market.dto.ProductDto;
import com.conel.market.dto.ProductResponseDto;
import com.conel.market.models.Category;
import com.conel.market.models.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {
    //WHAT ENTITY NEEDS
    public Product toProduct(ProductDto dto){
        var product=new Product();
        product.setName(dto.name());
        product.setStockQuantity(dto.stockQuantity());
        product.setPrice(dto.price());
        product.setDescription(dto.description());

        var category=new Category();
        category.setId(dto.categoryId());
        product.setCategory(category);//Linking
        return product;
    }

    //WHAT THE USER NEEDS TO SEE
    public ProductResponseDto toProductResponseDto(Product product){
        String baseUrl="http://localhost:8080/uploads/";
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                //If category exists ,get its name , else , null
                product.getCategory()!=null?product.getCategory().getName():null,
                product.getImageUrl()!=null?baseUrl+product.getImageUrl():null,
                product.getCreatedAt()
        );
    }
}
