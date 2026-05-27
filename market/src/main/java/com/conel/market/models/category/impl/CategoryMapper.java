package com.conel.market.models.category.impl;

import com.conel.market.models.category.Category;
import com.conel.market.models.category.dto.request.CategoryRequest;
import com.conel.market.models.category.dto.request.CategoryUpdateRequest;
import com.conel.market.models.category.dto.response.CategoryResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {
    //WHAT THE ENTITY NEEDS
    public Category toCategory(CategoryRequest dto){
        var category=new Category();
        category.setName(dto.name());
        return category;
    }

    public void mergeCategory(final Category categoryToUpdate, final CategoryUpdateRequest request) {
        if (StringUtils.isNotBlank(request.name())
                && !categoryToUpdate.getName()
                .equals(request.name())) {
            categoryToUpdate.setName(request.name());
        }
        if (StringUtils.isNotBlank(request.description())
                && !categoryToUpdate.getDescription()
                .equals(request.description())) {
            categoryToUpdate.setDescription(request.description());
        }
    }

    //WHAT THE USER NEEDS TO SEE
    public CategoryResponse toCategoryResponseDto(Category category){
        return new CategoryResponse(category.getId(),category.getName(), category.getDescription(), category.getCreatedAt(),category.getCreatedBy());
    }
}
