package com.conel.market.models.category.impl;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.models.category.Category;
import com.conel.market.models.category.CategoryRepository;
import com.conel.market.models.category.CategoryService;
import com.conel.market.models.category.dto.request.CategoryRequest;
import com.conel.market.models.category.dto.request.CategoryUpdateRequest;
import com.conel.market.models.category.dto.response.CategoryResponse;
import com.conel.market.models.products.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(final CategoryRequest request) {
        checkCategoryGlobalUnicity(request.name());

        final Category category = this.categoryMapper.toCategory(request);
        final Category savedCategory = this.categoryRepository.save(category);

        return this.categoryMapper.toCategoryResponseDto(savedCategory);
    }

    @Override
    @Transactional
    public void updateCategory(CategoryUpdateRequest request, String catId) {
        final Category categoryToUpdate = this.categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("No category found with id: " + catId));

        if (!categoryToUpdate.getName().equalsIgnoreCase(request.name())) {
            checkCategoryGlobalUnicity(request.name());
        }

        this.categoryMapper.mergeCategory(categoryToUpdate, request);
        this.categoryRepository.save(categoryToUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAllCategories(Pageable pageable) {
        return this.categoryRepository.findAll(pageable)
                .map(this.categoryMapper::toCategoryResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findCategoryById(String catId) {
        return this.categoryRepository.findById(catId)
                .map(this.categoryMapper::toCategoryResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("No category found with id: " + catId));
    }

    @Override
    @Transactional
    public void deleteCategoryById(String catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + catId));

        category.setActive(false);
        productRepository.archiveAllByCategoryId(catId);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void restoreCategory(String id) {
        Category archivedCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        checkCategoryGlobalUnicity(archivedCategory.getName());
        categoryRepository.restoreById(id);
    }

    private void checkCategoryGlobalUnicity(String name) {
        final boolean alreadyExists = this.categoryRepository.existsByNameIgnoreCase(name);
        if (alreadyExists) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS_FOR_USER);
        }
    }
}