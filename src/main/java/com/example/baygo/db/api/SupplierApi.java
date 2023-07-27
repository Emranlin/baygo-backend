package com.example.baygo.db.api;

import com.example.baygo.db.dto.request.SupplierRequest;
import com.example.baygo.db.dto.response.SimpleResponse;
import com.example.baygo.db.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/carPass")
@RequiredArgsConstructor
@Tag(name = "Product Seller API")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupplierApi {

    private final SupplierService service;

    @Operation(summary = "Create the supplier", description = "This method creating the supplier")
    @PostMapping("{supplyId}")
    @PreAuthorize("hasAuthority('SELLER')")
    SimpleResponse create(SupplierRequest supplierRequest,@PathVariable Long supplyId){
        return service.save(supplierRequest, supplyId);
    }
}
