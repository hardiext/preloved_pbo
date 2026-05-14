package com.preloved_id.preloved.controller;

import com.preloved_id.preloved.dto.ProductRequest;
import com.preloved_id.preloved.dto.ProductResponse;
import com.preloved_id.preloved.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;
@PostMapping(consumes = { org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
public ResponseEntity<ProductResponse> createProduct(
        @ModelAttribute ProductRequest request, // Spring akan memetakan name, price, dll ke sini
        @RequestParam("image") MultipartFile image,
        Authentication authentication 
) throws IOException {

    // Ambil email dari Security Context
    String email = authentication.getName();

    // 1. Simpan File
    String uploadDir = "uploads/";
    File directory = new File(uploadDir);
    if (!directory.exists()) {
        directory.mkdirs();
    }

    String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
    Path filePath = Paths.get(uploadDir + fileName);
    Files.write(filePath, image.getBytes());

    // 2. Set path gambar ke DTO
    request.setImageUrl(filePath.toString());

    return ResponseEntity.ok(
            productService.createProduct(request, email)
    );
}

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(

            @PathVariable Long id,

            @RequestBody ProductRequest request

    ) {

        return ResponseEntity.ok(
                productService.updateProduct(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id
    ) {

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                "Product deleted successfully"
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getByCategory(

            @PathVariable String category

    ) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(category)
        );
    }
}