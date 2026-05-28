package com.conel.market.models.category.impl;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.models.category.Category;
import com.conel.market.models.category.CategoryRepository;
import com.conel.market.models.category.CategoryService;
import com.conel.market.models.category.dto.request.CategoryRequest;
import com.conel.market.models.category.dto.request.CategoryUpdateRequest;
import com.conel.market.models.category.dto.response.CategoryResponse;
import com.conel.market.repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    @Override
    public String createCategory(final CategoryRequest request) {
        checkCategoryGlobalUnicity(request.name());

        final Category category = this.categoryMapper.toCategory(request);
        return this.categoryRepository.save(category).getId();
    }

    @Override
    public void updateCategory(CategoryUpdateRequest request, String catId) {
        final Category categoryToUpdate = this.categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("No category found with id: " + catId));

        // Only run a unicity check if they are actually changing the category's name
        if (!categoryToUpdate.getName().equalsIgnoreCase(request.name())) {
            checkCategoryGlobalUnicity(request.name());
        }

        this.categoryMapper.mergeCategory(categoryToUpdate, request);
        this.categoryRepository.save(categoryToUpdate);
    }

    @Override
    public List<CategoryResponse> findAllCategories() {
        return this.categoryRepository.findAll()
                .stream()
                .map(this.categoryMapper::toCategoryResponseDto)
                .toList();
    }


    @Override
    public CategoryResponse findCategoryById(String catId) {
        return this.categoryRepository.findById(catId)
                .map(this.categoryMapper::toCategoryResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("No category found with id: " + catId));
    }

    @Override
    public void deleteCategoryById(String catId) {
        Category category=categoryRepository.findById(catId)
                .orElseThrow(()->new EntityNotFoundException("category not found"));
        category.setActive(false);
        productRepository.archiveAllByCategoryId(catId);
        categoryRepository.save(category);
    }

    @Transactional
    public void RestoreCategory(String id){
        categoryRepository.restoreById(id);
    }

    private void checkCategoryGlobalUnicity(String name) {
        final boolean alreadyExists = this.categoryRepository.existsByNameIgnoreCase(name);
        if (alreadyExists) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS_FOR_USER);
        }
    }
}