package com.example.baygo.db.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ProductResponseForSeller(
        Long productId,
        Long subProductId,
        String image,
        String vendorNumber,
        String productArticle,
        String product,
        String brandName,
        double rating,
        LocalDate dateOfChange,
        String color,
        List<SizeSellerResponse> sizes
) {
}