package com.conel.market.service.category;

import com.conel.market.dto.category.request.CategoryRequest;
import com.conel.market.dto.category.request.CategoryUpdateRequest;
import com.conel.market.dto.category.response.CategoryResponse;
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