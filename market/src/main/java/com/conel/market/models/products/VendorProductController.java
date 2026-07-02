package com.conel.market.models.products;

import com.conel.market.file.FileStorageService;
import com.conel.market.models.products.dto.ProductRequest;
import com.conel.market.models.products.dto.ProductResponse;
import com.conel.market.models.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/vendors/products")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('product:create')")
public class VendorProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<Page<ProductResponse>> getMyProducts(
            @AuthenticationPrincipal User authenticatedUser,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ProductResponse> products = productService.getVendorProducts(authenticatedUser.getId(), pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ProductResponse> getMyProduct(
            @PathVariable String productId,
            @AuthenticationPrincipal User authenticatedUser) {
        ProductResponse product = productService.getVendorProduct(productId, authenticatedUser.getId());
        return ResponseEntity.ok(product);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser) {
        String filename = fileStorageService.saveFile(file);
        ProductResponse response = productService.saveProduct(request, filename, authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser) {
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.saveFile(file);
        }
        ProductResponse response = productService.updateVendorProduct(productId, request, fileName, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String productId,
            @AuthenticationPrincipal User authenticatedUser) {
        productService.deleteVendorProduct(productId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}