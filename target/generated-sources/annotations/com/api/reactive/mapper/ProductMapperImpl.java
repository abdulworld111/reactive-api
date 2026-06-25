package com.api.reactive.mapper;

import com.api.reactive.dto.request.ProductRequest;
import com.api.reactive.dto.response.ProductResponse;
import com.api.reactive.entity.Product;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-24T16:23:30+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.id( product.getId() );
        productResponse.name( product.getName() );
        productResponse.description( product.getDescription() );
        productResponse.price( product.getPrice() );
        productResponse.stock( product.getStock() );
        productResponse.category( product.getCategory() );
        productResponse.sku( product.getSku() );
        productResponse.active( product.getActive() );
        productResponse.createdAt( product.getCreatedAt() );
        productResponse.updatedAt( product.getUpdatedAt() );

        return productResponse.build();
    }

    @Override
    public Product toEntity(ProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.name( request.getName() );
        product.description( request.getDescription() );
        product.price( request.getPrice() );
        product.stock( request.getStock() );
        product.category( request.getCategory() );
        product.sku( request.getSku() );
        product.active( request.getActive() );

        return product.build();
    }

    @Override
    public void updateEntity(ProductRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            product.setDescription( request.getDescription() );
        }
        if ( request.getPrice() != null ) {
            product.setPrice( request.getPrice() );
        }
        if ( request.getStock() != null ) {
            product.setStock( request.getStock() );
        }
        if ( request.getCategory() != null ) {
            product.setCategory( request.getCategory() );
        }
        if ( request.getSku() != null ) {
            product.setSku( request.getSku() );
        }
        if ( request.getActive() != null ) {
            product.setActive( request.getActive() );
        }
    }
}
