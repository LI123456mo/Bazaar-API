package com.conel.market.models.products;

import com.conel.market.models.category.Category;
import com.conel.market.models.category.CategoryRepository;
import com.conel.market.models.products.dto.ProductRequest;
import com.conel.market.models.products.dto.ProductResponse;
import com.conel.market.specifications.ProductSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse saveProduct(ProductRequest dto, String fileName, String userId) {
        var product = productMapper.toProduct(dto);
        product.setImageUrl(fileName);

        // Track who owns this listing behind the scenes
        // product.setSellerId(userId); // Uncomment once your User/Seller field mapping is ready!

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.categoryId()));
            product.setCategory(category);
        }

        var savedProduct = productRepository.save(product);
        return productMapper.toProductResponseDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest dto, String newFileName) {
        Product existingProduct = getProductEntity(id);

        if (newFileName != null) {
            String oldFileName = existingProduct.getImageUrl();
            if (oldFileName != null) {
                try {
                    Path oldFilePath = Paths.get("uploads").resolve(oldFileName);
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    log.warn("Could not delete old file from file system: {}", e.getMessage());
                }
            }
            existingProduct.setImageUrl(newFileName);
        }

        existingProduct.setName(dto.name());
        existingProduct.setDescription(dto.description());
        existingProduct.setPrice(dto.price());
        existingProduct.setStockQuantity(dto.stockQuantity());

        if (dto.categoryId() != null && !dto.categoryId().equals(existingProduct.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.categoryId()));
            existingProduct.setCategory(newCategory);
        }

        Product savedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(String id) {
        return productRepository.findById(id)
                .map(productMapper::toProductResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {
        return productRepository.findAllByNameContainingIgnoreCase(name)
                .stream()
                .map(productMapper::toProductResponseDto)
                .toList();
    }


    @Transactional
    public void decreaseStock(String productId, Integer quantity) {
        Product product = getProductEntity(productId);

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock remaining for product: " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse increaseStock(String productId, Integer quantity) {
        Product product = getProductEntity(productId);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productMapper.toProductResponseDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public boolean isProductAvailable(String productId) {
        Product product = getProductEntity(productId);
        return product.isActive() && product.getStockQuantity() != null && product.getStockQuantity() > 0;
    }

    @Transactional(readOnly = true)
    public Product getProductEntity(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, Double maxPrice, String category, Pageable pageable) {
        Specification<Product> spec = Specification.where((root, query, cb) -> cb.equal(root.get("active"), true));

        if (name != null && !name.isEmpty()) {
            spec = spec.and(ProductSpecification.nameLike(name));
        }
        if (category != null && !category.isEmpty()) {
            spec = spec.and(ProductSpecification.hasCategoryName(category));
        }
        if (maxPrice != null && maxPrice > 0) {
            spec = spec.and(ProductSpecification.priceLessThan(maxPrice));
        }

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toProductResponseDto);
    }

    @Transactional
    public void deleteProduct(String id) {
        Product product = getProductEntity(id);
        // CHANGED: Soft delete implementation ensures orders remain intact
        product.setActive(false);
        productRepository.save(product);
    }
}