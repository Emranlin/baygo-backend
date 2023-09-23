package com.example.baygo.api.seller;

import com.example.baygo.db.dto.response.PaginationResponse;
import com.example.baygo.db.dto.response.orders.AnalysisResponse;
import com.example.baygo.db.dto.response.orders.FBBOrderResponse;
import com.example.baygo.db.model.enums.OrderStatus;
import com.example.baygo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/sellers/orders")
@RequiredArgsConstructor
@Tag(name = "Seller order")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAuthority('SELLER')")
public class SellerOrderController {
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "This method will get all orders and filter by status and keyword")
    public PaginationResponse<FBBOrderResponse> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.getAllOrdersByFilter(page, size, keyword, status,categoryId);
    }

    @Operation(summary = "Get weekly analysis", description = "This method will get week analysis and filter ")
    @GetMapping("/analysis")
    AnalysisResponse weeklyAnalysisOrder(
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String nameOfTime) {
        return orderService.getWeeklyAnalysis(startDate, endDate, warehouseId, nameOfTime);
    }
}
