package com.conel.market.models.category;

import com.conel.market.models.category.dto.request.CategoryRequest;
import com.conel.market.models.category.dto.request.CategoryUpdateRequest;
import com.conel.market.models.category.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    String createCategory(CategoryRequest request);

    void updateCategory(CategoryUpdateRequest request, String catId);

    List<CategoryResponse> findAllCategories();

    CategoryResponse findCategoryById(String catId);

    void deleteCategoryById(String catId);
}
