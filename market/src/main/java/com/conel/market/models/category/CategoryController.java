package com.conel.market.models.category;

import com.conel.market.models.category.dto.request.CategoryRequest;
import com.conel.market.models.category.dto.request.CategoryUpdateRequest;
import com.conel.market.models.category.dto.response.CategoryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category Management API")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody final CategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.categoryService.createCategory(request));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(this.categoryService.findAllCategories(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable final String id) {
        return ResponseEntity.ok(this.categoryService.findCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable final String id,
            @Valid @RequestBody final CategoryUpdateRequest request
    ) {
        this.categoryService.updateCategory(request, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable final String id) {
        this.categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreCategory(@PathVariable final String id) {
        this.categoryService.restoreCategory(id);
        return ResponseEntity.noContent().build(); // 204 No Content confirms execution cleanly
    }
}