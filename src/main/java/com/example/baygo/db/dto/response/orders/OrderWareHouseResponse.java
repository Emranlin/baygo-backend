package com.example.baygo.db.dto.response.orders;

import lombok.Builder;

@Builder
public record OrderWareHouseResponse(
        String location,
        double percentage
) {
}
