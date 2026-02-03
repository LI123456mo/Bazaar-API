package com.conel.market.controllers;

import com.conel.market.dto.ProductDto;
import com.conel.market.dto.ProductResponseDto;
import com.conel.market.repositories.ProductRepository;
import com.conel.market.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<ProductResponseDto> create(@RequestBody ProductDto productDto){
        ProductResponseDto savedProduct=productService.saveProduct(productDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedProduct);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ProductResponseDto findById(@PathVariable Integer id){
        return productService.findById(id);
    }
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page, // Default to first page
            @RequestParam(defaultValue = "10") int size, // Default to 10 items
            @RequestParam(defaultValue = "id,asc") String sort // Default sort
    ) {
        //  building the Pageable object here
        Pageable pageable = PageRequest.of(page, size,(parseSort(sort)));

        Page<ProductResponseDto> results=productService.searchProducts(name,maxPrice,category,pageable);

        return ResponseEntity.ok(results);
    }

    @PutMapping("/{productId}/increase")
    public ProductResponseDto updateStock(@PathVariable Integer productId,@RequestParam Integer quantity){
        return productService.increaseStock(productId,quantity);
    }

    private  Sort parseSort(String sort){
        String[] parts=sort.split(",");
        String property=parts[0];
        String direction=(parts.length>1)?parts[1]:"asc";

        return direction.equalsIgnoreCase("desc")
                ?Sort.by(property).descending()
                :Sort.by(property).ascending();
    }
}
