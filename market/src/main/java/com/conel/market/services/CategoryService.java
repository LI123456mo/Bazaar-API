package com.conel.market.services;

import com.conel.market.dto.CategoryDto;
import com.conel.market.dto.CategoryResponseDto;
import com.conel.market.mapper.CategoryMapper;
import com.conel.market.models.Category;
import com.conel.market.models.Product;
import com.conel.market.repositories.CategoryRepository;
import com.conel.market.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    //SAVE
           //The output                       //The input
    public CategoryResponseDto saveCategory(CategoryDto dto){
        var category=categoryMapper.toCategory(dto);
        if (categoryRepository.existsByNameAndDeletedFalse(category.getName())){
            throw new RuntimeException("An active category with the name '" + dto.name() + "' already exists!");
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

    @Transactional
    public void deleteCategory(Integer id){
        Category category=categoryRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Category not found"));

        if (category.getProducts()!=null){
            for(Product product:category.getProducts()){
                productRepository.delete(product);
            }
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public void RestoreCategory(Integer id){
        Category category=categoryRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Category not found in archives"));

        category.setDeleted(false);
        categoryRepository.save(category);
    }
}
