package com.conel.market.controller;

import com.conel.market.file.FileStorageService;
import com.conel.market.service.product.ProductService;
import com.conel.market.dto.product.request.ProductRequest;
import com.conel.market.dto.product.response.ProductResponse;
import com.conel.market.user.entity.User;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;



    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAuthority('product:create')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart("file")MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser
            ){
        String filename=fileStorageService.saveFile(file);

        ProductResponse response=productService.saveProduct(request,filename, authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAuthority('product:update')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") String id,
            @RequestPart("product") ProductRequest dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            fileName = fileStorageService.saveFile(file);
        }
        ProductResponse response = productService.updateProduct(id, dto, fileName,authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ProductResponse> findById(@PathVariable String id){
        ProductResponse response =productService.findById(id);
        return ResponseEntity.ok(response);
    }



    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ProductResponse> products = productService.searchProducts(name, maxPrice, category, pageable);
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
}