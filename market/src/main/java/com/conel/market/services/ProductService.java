package com.conel.market.services;

import com.conel.market.dto.ProductDto;
import com.conel.market.dto.ProductResponseDto;
import com.conel.market.mapper.ProductMapper;
import com.conel.market.models.Category;
import com.conel.market.models.Product;
import com.conel.market.repositories.CategoryRepository;
import com.conel.market.repositories.ProductRepository;
import com.conel.market.specifications.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    //SAVE
    public ProductResponseDto saveProduct(ProductDto dto,String fileName){
        var product=productMapper.toProduct(dto);
        product.setImageUrl(fileName);

        if (dto.categoryId()!=null){
            Category category=categoryRepository.findById(dto.categoryId())
                    .orElseThrow(()->new RuntimeException("Category not found with id: "+dto.categoryId()));

            product.setCategory(category);
        }

        var saveProduct=productRepository.save(product);
        return productMapper.toProductResponseDto(saveProduct);
    }

    public void updateProduct(Integer id,Product updateDetails){
        Product existingProduct=getProductEntity(id);

        //updates the details
        existingProduct.setName(updateDetails.getName());
        existingProduct.setPrice(updateDetails.getPrice());
        existingProduct.setDescription(updateDetails.getDescription());

        productRepository.save(existingProduct);
    }
    public ProductResponseDto findById(Integer id){
        return productRepository.findById(id)
                .map(productMapper::toProductResponseDto)
                .orElseThrow(()-> new RuntimeException("Product not found! withi id:"+ id));
    }

    public List<ProductResponseDto> searchByName(String name){
        return productRepository.findAllByNameContainingIgnoreCase(name)
                .stream()
                .map(productMapper::toProductResponseDto)
                .toList();
    }

    //Decreasing stock
    @Transactional
    public void decreaseStock(Integer productId,Integer quantity){
        Product product=getProductEntity(productId);

        if (product.getStockQuantity()<quantity){
            throw new RuntimeException("Not enough stock for product: "+product.getName());
        }
        product.setStockQuantity(product.getStockQuantity()-quantity);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponseDto increaseStock(Integer productId,Integer quantity){
        Product product=getProductEntity(productId);
        product.setStockQuantity(product.getStockQuantity()+quantity);
        //productRepository.save(product);
        return productMapper.toProductResponseDto(productRepository.save(product));
    }

    public boolean isProductAvailable(Integer productId){
        Product product=getProductEntity(productId);
        return product.getStockQuantity()!=null && product.getStockQuantity()>0;
    }

    public Product getProductEntity(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Page<ProductResponseDto> searchProducts(String name, Double maxPrice, String category, Pageable pageable){
        Specification<Product> spec=Specification
                .where(ProductSpecification.nameLike(name))
                .and(ProductSpecification.hasCategoryName(category))
                .and(ProductSpecification.priceLessThan(maxPrice));
        return productRepository.findAll(spec,pageable)
                .map(productMapper::toProductResponseDto);
    }
}
