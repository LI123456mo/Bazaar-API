package com.conel.market.controller;

import com.conel.market.file.FileStorageService;
import com.conel.market.service.product.ProductService;
import com.conel.market.dto.product.request.ProductRequest;
import com.conel.market.dto.product.response.ProductResponse;
import com.conel.market.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAuthority('product:create')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser) {

        String filename = fileStorageService.saveFile(file);
        ProductResponse response = productService.saveProduct(request, filename, authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAuthority('product:update')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") String id,
            @Valid @RequestPart("product") ProductRequest dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        String fileName = (file != null && !file.isEmpty()) ? fileStorageService.saveFile(file) : null;
        ProductResponse response = productService.updateProduct(id, dto, fileName, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ProductResponse> findById(@PathVariable String id){
        ProductResponse response =productService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Pageable safePageable = capPageSize(pageable, 100);
        Page<ProductResponse> products = productService.searchProducts(name, maxPrice, category, safePageable);
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product:delete')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id,
            @AuthenticationPrincipal User authenticatedUser
    ){
        productService.deleteProduct(id,authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/restock")
    @PreAuthorize("hasAuthority('product:update')")
    public ResponseEntity<ProductResponse> restock(
            @PathVariable String productId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(productService.increaseStock(productId, quantity));
    }

    private Pageable capPageSize(Pageable pageable, int max) {
        int size = Math.min(pageable.getPageSize(), max);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}