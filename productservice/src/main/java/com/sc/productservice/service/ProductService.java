package com.sc.productservice.service;

import com.sc.productservice.model.ProductRequest;
import com.sc.productservice.model.ProductResponse;

public interface ProductService {

    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    public void reduceQuantity(long productId, long quantity);

}
