package com.conel.market.services;

import com.conel.market.dto.CategoryDto;
import com.conel.market.dto.CategoryResponseDto;
import com.conel.market.mapper.CategoryMapper;
import com.conel.market.models.Category;
import com.conel.market.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    //SAVE
           //The output                       //The input
    public CategoryResponseDto saveCategory(CategoryDto dto){
        var category=categoryMapper.toCategory(dto);
        if (categoryRepository.existsByName(category.getName())){
            throw new RuntimeException("Category already exists!");
        }
        var saveCategory=categoryRepository.save(category);
        return categoryMapper.tocategoryResponseDto(saveCategory);
    }

    //SEARCHING
    public List<CategoryResponseDto> findAll(){
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::tocategoryResponseDto)
                .toList();
    }

    //SEARCHING
    public CategoryResponseDto findById(Integer id){
        return categoryRepository.findById(id)
                .map(categoryMapper::tocategoryResponseDto)
                .orElseThrow(()->new RuntimeException("Category not found with id: " + id));
    }
}
