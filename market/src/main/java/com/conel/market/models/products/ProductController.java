package com.conel.market.models.products;

import com.conel.market.file.FileStorageService;
import com.conel.market.models.products.dto.ProductRequest;
import com.conel.market.models.products.dto.ProductResponse;
import com.conel.market.models.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart("file")MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser
            ){
        String filename=fileStorageService.saveFile(file);

        String currentUserId= (authenticatedUser!=null)?authenticatedUser.getId():"system-merchant";
        ProductResponse response=productService.saveProduct(request,filename,currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
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
    public ResponseEntity<ProductResponse> findById(@PathVariable String id){
        ProductResponse response =productService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(required = false)String name,
            @RequestParam(required = false)Double maxPrice,
            @RequestParam(required = false)String category,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size,
            @RequestParam(defaultValue = "id,asc")String sort
    ){
        Pageable pageable=PageRequest.of(page,size,parseSort(sort));
        Page<ProductResponse> results=productService.searchProducts(name,maxPrice,category,pageable);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable("id")String id
    ){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSort(String sort){
        String[] parts=sort.split(",");
        String property=parts[0];
        String direction=(parts.length>1)?parts[1]:"asc";

        return direction.equalsIgnoreCase("desc")
                ?Sort.by(property).descending()
                :Sort.by(property).ascending();
    }
}