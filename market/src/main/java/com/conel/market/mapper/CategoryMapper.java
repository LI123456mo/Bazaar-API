package com.conel.market.mapper;

import com.conel.market.entity.category.Category;
import com.conel.market.dto.category.request.CategoryRequest;
import com.conel.market.dto.category.request.CategoryUpdateRequest;
import com.conel.market.dto.category.response.CategoryResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {
    //WHAT THE ENTITY NEEDS
    public Category toCategory(CategoryRequest dto){
        var category = new Category();
        category.setName(dto.name());
        category.setDescription(dto.description()); // 👑 FIXED: Maps the description field correctly!
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
