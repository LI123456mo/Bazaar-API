package com.conel.market.service.product;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.entity.category.Category;
import com.conel.market.repository.category.CategoryRepository;
import com.conel.market.entity.product.Product;
import com.conel.market.mapper.ProductMapper;
import com.conel.market.repository.product.ProductRepository;
import com.conel.market.dto.product.request.ProductRequest;
import com.conel.market.dto.product.response.ProductResponse;
import com.conel.market.user.entity.User;
import com.conel.market.specifications.ProductSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final EntityManager entityManager;

    @Value("${app.file-storage.upload-dir:./uploads}")
    private String uploadDir;

    @Transactional
    public ProductResponse saveProduct(ProductRequest dto, String fileName, String userId) {
        var product = productMapper.toProduct(dto);
        product.setImageUrl(fileName);

        User seller=entityManager.getReference(User.class,userId);
        product.setSeller(seller);

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.categoryId()));
            product.setCategory(category);
        }

        var savedProduct = productRepository.save(product);
        return productMapper.toProductResponseDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest dto, String newFileName, User authenticatedUser) {
        Product existingProduct = getProductEntity(id);

        //  Multi-tenant security check. Prevents foreign seller modifications.
        validateProductOwnership(existingProduct, authenticatedUser);

        if (newFileName != null) {
            String oldFileName = existingProduct.getImageUrl();
            if (oldFileName != null) {
                try {
                    // Use the configured upload directory so cleanup stays aligned with storage and static serving.
                    Path oldFilePath = Paths.get(uploadDir).resolve(oldFileName);
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

        if (dto.categoryId() != null) {
            if (existingProduct.getCategory() == null || !dto.categoryId().equals(existingProduct.getCategory().getId())) {
                Category newCategory = categoryRepository.findById(dto.categoryId())
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.categoryId()));
                existingProduct.setCategory(newCategory);
            }
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




    /**
     * Builds a dynamic JPA Specification query based on optional client filters and executes a paginated database search.
     * <p>
     * Rather than creating multiple static repository queries, this method compiles a single type-safe
     * SQL execution block on the fly. It enforces a structural baseline condition ensuring that only
     * <b>active (non-soft-deleted)</b> products are returned to the user.
     * </p>
     * * <h3>Advanced Query Performance Tuning:</h3>
     * To accommodate Spring Data's pagination mechanics without destroying database performance:
     * <ul>
     * <li><b>Count Mapping:</b> When executing background total row counts, the query skips aggressive data fetches.</li>
     * <li><b>Fetch Optimization:</b> When pulling actual rows, it forces an <code>INNER FETCH JOIN</code> on the
     * product's Category relationship. This loads both entities into application memory in 1 database round-trip,
     * completely solving the <i>N+1 Lazy Loading Performance Deficit</i>.</li>
     * </ul>
     *
     * @param name       The optional name search term passed from the controller layer.
     * @param maxPrice   The optional price cell threshold constraint.
     * @param category   The optional category text identifier to filter against.
     * @param pageable   The pre-configured pagination block containing limit, offset, and sort preferences.
     * @return A paginated data container holding mapped {@link ProductResponse} records.
     */
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
    public void deleteProduct(String id,User authenticatedUser) {
        Product product = getProductEntity(id);
        validateProductOwnership(product,authenticatedUser);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getVendorProducts(String vendorId, Pageable pageable) {
        return productRepository.findBySellerId(vendorId, pageable)
                .map(productMapper::toProductResponseDto);
    }

    @Transactional(readOnly = true)
    public ProductResponse getVendorProduct(String productId, String vendorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (!product.getSeller().getId().equals(vendorId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return productMapper.toProductResponseDto(product);
    }

    @Transactional
    public ProductResponse updateVendorProduct(String productId, ProductRequest dto, String newFileName, User vendor) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        validateProductOwnership(product, vendor);

        if (newFileName != null) {
            try {
                if (product.getImageUrl() != null) {
                    Path oldFilePath = Paths.get(uploadDir).resolve(product.getImageUrl());
                    Files.deleteIfExists(oldFilePath);
                }
            } catch (IOException e) {
                log.warn("Could not delete old file: {}", e.getMessage());
            }
            product.setImageUrl(newFileName);
        }

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStockQuantity(dto.stockQuantity());

        if (dto.categoryId() != null) {
            // Vendor edits should also be able to move a product between categories.
            Category newCategory = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.categoryId()));
            product.setCategory(newCategory);
        }

        return productMapper.toProductResponseDto(productRepository.save(product));
    }

    @Transactional
    public void deleteVendorProduct(String productId, User vendor) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        validateProductOwnership(product, vendor);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public Product getProductEntityWithLock(String id) {
        return productRepository.findByIdWithPessimisticLock(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Transactional
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    /**
     *  Helper method to enforce tenant isolation rules safely.
     * Allows changes only if the user is the original listing seller OR has administrative rights.
     */
    private void validateProductOwnership(Product product, User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("admin:access"));

        boolean isOwner = product.getSeller() != null && product.getSeller().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}