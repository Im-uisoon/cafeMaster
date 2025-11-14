package com.project.summer.domain.product;

import com.project.summer.domain.product.dto.ProductCreateRequestDto;
import com.project.summer.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public Product createProduct(ProductCreateRequestDto requestDto) {
        if (productRepository.existsByProductCode(requestDto.getProductCode())) {
            throw new IllegalArgumentException("이미 존재하는 상품 코드입니다.");
        }
        if (productRepository.existsByProductName(requestDto.getProductName())) {
            throw new IllegalArgumentException("이미 존재하는 상품명입니다.");
        }

        Product product = Product.builder()
                .productCode(requestDto.getProductCode())
                .productName(requestDto.getProductName())
                .price(requestDto.getPrice())
                .build();
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상품을 찾을 수 없습니다."));
    }

    // 상품 정보 수정 (예시: 상품명, 가격)
    @Transactional
    public Product updateProduct(Long id, ProductCreateRequestDto requestDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상품을 찾을 수 없습니다."));

        // 상품 코드 중복 검사 (자신이 아닌 다른 상품과 중복되는지)
        if (!product.getProductCode().equals(requestDto.getProductCode()) &&
                productRepository.existsByProductCode(requestDto.getProductCode())) {
            throw new IllegalArgumentException("이미 사용 중인 상품 코드입니다.");
        }
        // 상품명 중복 검사 (자신이 아닌 다른 상품과 중복되는지)
        if (!product.getProductName().equals(requestDto.getProductName()) &&
                productRepository.existsByProductName(requestDto.getProductName())) {
            throw new IllegalArgumentException("이미 사용 중인 상품명입니다.");
        }

        Product updatedProduct = Product.builder()
                .id(product.getId())
                .productCode(requestDto.getProductCode())
                .productName(requestDto.getProductName())
                .price(requestDto.getPrice())
                .build();
        return productRepository.save(updatedProduct);
    }

    // 상품 삭제
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 상품을 찾을 수 없습니다.");
        }
        productRepository.deleteById(id);
    }
}
