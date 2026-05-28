package com.conel.market.controllers;

import com.conel.market.models.category.CategoryService;
import com.conel.market.models.category.dto.request.CategoryRequest;
import com.conel.market.models.category.dto.request.CategoryUpdateRequest;
import com.conel.market.models.category.dto.response.CategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@RequestBody @Valid final CategoryRequest request){
        return new ResponseEntity<>(this.categoryService.createCategory(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(
            @RequestBody @Valid final CategoryUpdateRequest request,
            @PathVariable("id") final String id
            ){
        this.categoryService.updateCategory(request,id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(){
        return ResponseEntity.ok(this.categoryService.findAllCategories());
    }

    @GetMapping
    public ResponseEntity<CategoryResponse> findById(String id){
        return ResponseEntity.ok(this.categoryService.findCategoryById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Restricts soft-deletion to admins only
    public ResponseEntity<Void> delete(
            @PathVariable("id") final String id
    ) {
        this.categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
