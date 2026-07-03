package com.conel.market.controller;

import com.conel.market.service.category.CategoryService;
import com.conel.market.dto.category.request.CategoryRequest;
import com.conel.market.dto.category.request.CategoryUpdateRequest;
import com.conel.market.dto.category.response.CategoryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category Management API")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('category:create')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody final CategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.categoryService.createCategory(request));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(this.categoryService.findAllCategories(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable final String id) {
        return ResponseEntity.ok(this.categoryService.findCategoryById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<Void> updateCategory(
            @PathVariable final String id,
            @Valid @RequestBody final CategoryUpdateRequest request
    ) {
        this.categoryService.updateCategory(request, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable final String id) {
        this.categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('category:delete')")
    public ResponseEntity<Void> restoreCategory(@PathVariable final String id) {
        this.categoryService.restoreCategory(id);
        return ResponseEntity.noContent().build(); // 204 No Content confirms execution cleanly
    }
}