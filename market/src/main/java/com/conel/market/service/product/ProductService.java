package com.conel.market.service.product;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.entity.category.Category;
import com.conel.market.file.FileStorageService;
import com.conel.market.repository.category.CategoryRepository;
import com.conel.market.entity.product.Product;
import com.conel.market.mapper.ProductMapper;
import com.conel.market.repository.product.ProductRepository;
import com.conel.market.dto.product.request.ProductRequest;
import com.conel.market.dto.product.response.ProductResponse;
import com.conel.market.user.entity.User;
import com.conel.market.specifications.ProductSpecification;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // CHANGE: added
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductResponse saveProduct(ProductRequest dto, String fileName, String userId) {
        var product = productMapper.toProduct(dto);
        product.setImageUrl(fileName);

        User seller = entityManager.getReference(User.class, userId);
        product.setSeller(seller);

        if (dto.categoryId() != null) {
            product.setCategory(getCategoryOrThrow(dto.categoryId()));
        }

        var savedProduct = productRepository.save(product);
        log.info("Product '{}' (id={}) created by seller {}", savedProduct.getName(), savedProduct.getId(), userId);
        return productMapper.toProductResponseDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest dto, String newFileName, User authenticatedUser) {
        Product existingProduct = getProductEntity(id);
        validateProductOwnership(existingProduct, authenticatedUser);

        if (newFileName != null) {
            String oldFileName = existingProduct.getImageUrl();
            if (oldFileName != null) {
                try {
                    fileStorageService.deleteFile(oldFileName);
                } catch (Exception e) {
                    log.warn("Could not delete old image '{}' for product {}: {}", oldFileName, id, e.getMessage());
                }
            }
            existingProduct.setImageUrl(newFileName);
        }

        existingProduct.setName(dto.name());
        existingProduct.setDescription(dto.description());
        existingProduct.setPrice(dto.price());
        existingProduct.setStockQuantity(dto.stockQuantity());

        if (dto.categoryId() != null
                && (existingProduct.getCategory() == null || !dto.categoryId().equals(existingProduct.getCategory().getId()))) {
            existingProduct.setCategory(getCategoryOrThrow(dto.categoryId()));
        }

        Product savedProduct = productRepository.save(existingProduct);
        log.info("Product {} updated by user {}", id, authenticatedUser.getId());
        return productMapper.toProductResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(String id) {
        return productMapper.toProductResponseDto(getProductEntity(id));
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
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK_QUANTITY);
        }
        Product product = getProductEntityWithLock(productId);

        if (product.getStockQuantity() < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
    }

    @Transactional
    public ProductResponse increaseStock(String productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK_QUANTITY);
        }
        Product product = getProductEntityWithLock(productId);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional
    public Product getProductEntityWithLock(String id) {
        return productRepository.findByIdWithPessimisticLock(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, BigDecimal maxPrice, String category, Pageable pageable) {
        Specification<Product> spec = Specification.where((root, query, cb) -> cb.equal(root.get("active"), true));

        if (name != null && !name.isEmpty()) {
            spec = spec.and(ProductSpecification.nameLike(name));
        }
        if (category != null && !category.isEmpty()) {
            spec = spec.and(ProductSpecification.hasCategoryName(category));
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            spec = spec.and(ProductSpecification.priceLessThan(maxPrice));
        }

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toProductResponseDto);
    }

    @Transactional
    public void deleteProduct(String id, User authenticatedUser) {
        Product product = getProductEntity(id);
        validateProductOwnership(product, authenticatedUser);
        product.setActive(false);
        log.info("Product {} deactivated by user {}", id, authenticatedUser.getId());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getVendorProducts(String vendorId, Pageable pageable) {
        return productRepository.findBySellerId(vendorId, pageable)
                .map(productMapper::toProductResponseDto);
    }

    @Transactional(readOnly = true)
    public ProductResponse getVendorProduct(String productId, String vendorId) {
        Product product = getProductEntity(productId);
        if (!product.getSeller().getId().equals(vendorId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return productMapper.toProductResponseDto(product);
    }

    @Transactional
    public void persistProductEntity(Product product) {
        productRepository.save(product);
    }

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

    private Category getCategoryOrThrow(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}