package com.conel.market.models.category;

import com.conel.market.models.category.dto.request.CategoryRequest;
import com.conel.market.models.category.dto.request.CategoryUpdateRequest;
import com.conel.market.models.category.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);

    void updateCategory(CategoryUpdateRequest request, String catId);


    Page<CategoryResponse> findAllCategories(Pageable pageable);

    CategoryResponse findCategoryById(String catId);

    void deleteCategoryById(String catId);

    void restoreCategory(String id);
}