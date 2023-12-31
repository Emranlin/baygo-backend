package com.example.baygo.repository.custom;

import com.example.baygo.db.dto.response.PaginationResponse;
import com.example.baygo.db.dto.response.SupplyLandingPage;
import com.example.baygo.db.dto.response.SupplyTransitDirectionResponse;
import com.example.baygo.db.dto.response.deliveryFactor.DeliveryFactorResponse;
import com.example.baygo.db.dto.response.deliveryFactor.WarehouseCostResponse;
import com.example.baygo.db.dto.response.supply.SupplyInfoResponse;
import com.example.baygo.db.model.enums.SupplyType;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SupplyCustomRepository {

    PaginationResponse<DeliveryFactorResponse> findAllDeliveryFactor(Long warehouseId, LocalDate date, int size, int page);

    List<SupplyTransitDirectionResponse> getAllTransitDirections(String transitWarehouse, String destinationWarehouse);

    List<SupplyLandingPage> getAllSupplyForLanding(Long sellerId);

    List<WarehouseCostResponse> getAllWarehouseCostResponse(Long warehouseId, SupplyType supplyType);
    List<SupplyInfoResponse> findById(Long supplyId);
}