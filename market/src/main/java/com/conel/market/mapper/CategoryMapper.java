package com.conel.market.mapper;

import com.conel.market.dto.CategoryDto;
import com.conel.market.dto.CategoryResponseDto;
import com.conel.market.models.Category;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {
    //WHAT THE ENTITY NEEDS
    public Category toCategory(CategoryDto dto){
        return Category.builder()
                .name(dto.name())
                .description(dto.description())
                .build();
    }
    //WHAT THE USER NEEDS TO SEE
    public CategoryResponseDto tocategoryResponseDto(Category category){
        return new CategoryResponseDto(category.getId(),category.getName(),category.getCreatedAt(),category.getCreatedBy(),category.getDescription());
    }
}
