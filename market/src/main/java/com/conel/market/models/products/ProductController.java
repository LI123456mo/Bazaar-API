package com.conel.market.models.products;

import com.conel.market.file.FileStorageService;
import com.conel.market.models.products.dto.ProductRequest;
import com.conel.market.models.products.dto.ProductResponse;
import com.conel.market.models.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart("file")MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser
            ){
        String filename=fileStorageService.saveFile(file);

        String currentUserId= (authenticatedUser!=null)?authenticatedUser.getId():"system-merchant";
        ProductResponse response=productService.saveProduct(request,filename,currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") String id,
            @RequestPart("product") ProductRequest dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            fileName = fileStorageService.saveFile(file);
        }
        ProductResponse response = productService.updateProduct(id, dto, fileName,authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable String id){
        ProductResponse response =productService.findById(id);
        return ResponseEntity.ok(response);
    }



    /**
     * REST endpoint to search and filter products with dynamic criteria and pagination.
     * <p>
     * This endpoint allows open, public access to the product catalog. Clients can optionally
     * filter results by product name, maximum price, or category name. Results are always
     * returned in a paginated format to protect server resources.
     * </p>
     *
     * <h3>Example Request:</h3>
     * <pre>
     * GET /api/v1/products?category=Electronics&amp;maxPrice=500.00&amp;page=0&amp;size=10&amp;sort=price,desc
     * </pre>
     *
     * @param name      Optional text string to search for in product names (case-insensitive partial match).
     * @param maxPrice  Optional upper threshold limit for product pricing. Must be greater than 0.
     * @param category  Optional exact text name of the category the product belongs to.
     * @param page      The zero-indexed page number to retrieve (defaults to 0).
     * @param size      The maximum number of products to return inside this page block (defaults to 10).
     * @param sort      The sorting parameters formatted as "field,direction" (defaults to "id,asc").
     * @return A {@link ResponseEntity} wrapping a {@link Page} of {@link ProductResponse} objects matching the criteria.
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(required = false)String name,
            @RequestParam(required = false)Double maxPrice,
            @RequestParam(required = false)String category,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size,
            @RequestParam(defaultValue = "id,asc")String sort
    ){
        Pageable pageable=PageRequest.of(page,size,parseSort(sort));
        Page<ProductResponse> results=productService.searchProducts(name,maxPrice,category,pageable);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable("id")String id,
            @AuthenticationPrincipal User authenticatedUser
    ){
        productService.deleteProduct(id,authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSort(String sort){
        String[] parts=sort.split(",");
        String property=parts[0];
        String direction=(parts.length>1)?parts[1]:"asc";

        return direction.equalsIgnoreCase("desc")
                ?Sort.by(property).descending()
                :Sort.by(property).ascending();
    }
}