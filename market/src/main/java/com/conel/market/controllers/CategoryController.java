package com.conel.market.controllers;

import com.conel.market.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryResponseDto create(@RequestBody CategoryRequest category){
        return categoryService.saveCategory(category);
    }

    @GetMapping
    public List<CategoryResponseDto> findAll(){
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public CategoryResponseDto findById(@PathVariable Integer id){
        return categoryService.findById(id);
    }
}
