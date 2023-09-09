package com.example.baygo.repository;

import com.example.baygo.db.dto.response.BuyerOrderHistoryDetailResponse;
import com.example.baygo.db.dto.response.BuyerOrderProductsResponse;
import com.example.baygo.db.dto.response.BuyerOrdersHistoryResponse;
import com.example.baygo.db.dto.response.fbs.FBSOrdersResponse;
import com.example.baygo.db.dto.response.orders.FBBOrderResponse;
import com.example.baygo.db.dto.response.orders.RecentOrdersResponse;
import com.example.baygo.db.model.Order;
import com.example.baygo.db.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Modifying
    @Query("DELETE FROM Order o WHERE o.id = :orderId ")
    void deleteById(@Param("orderId") Long orderId);

    @Query("SELECT NEW com.example.baygo.db.dto.response.orders.RecentOrdersResponse(o.id,sp.articulBG,p.name, sp.price, os.quantity ,os.orderStatus)" +
            " FROM Order o JOIN o.orderSizes os JOIN os.size s JOIN s.subProduct sp JOIN sp.product p WHERE p.seller.id = :sellerId order by o.id desc limit 5")
    List<RecentOrdersResponse> getAllRecentOrders(Long sellerId);

    @Query("SELECT NEW com.example.baygo.db.dto.response.orders.FBBOrderResponse(o.id, s.barcode,sp.mainImage,sp.articulOfSeller, p.name,os.quantity, b.fullName, sp.price, o.dateOfOrder, os.orderStatus) " +
            "FROM Order o " +
            "JOIN o.orderSizes os " +
            "JOIN os.size s " +
            "JOIN s.subProduct sp " +
            "JOIN sp.product p " +
            "JOIN o.buyer b " +
            "WHERE p.seller.id = :sellerId " +
            "AND os.isFbsOrder = false " +
            "AND (:keyword IS NULL OR p.name ILIKE %:keyword% OR b.fullName ILIKE %:keyword%) " +
            "AND (:status IS NULL OR os.orderStatus = :status) " +
            "ORDER BY o.dateOfOrder DESC")
    Page<FBBOrderResponse> getAllOrders(Long sellerId, String keyword, OrderStatus status, Pageable pageable);

    @Query("SELECT NEW com.example.baygo.db.dto.response.fbs.FBSOrdersResponse(" +
            "o.id, s.id, sp.mainImage, s.barcode, os.quantity, p.name, sp.articulOfSeller, s.size, " +
            "sp.color, sp.price, CONCAT(fw.street, ' ', fw.houseNumber), os.orderStatus, o.dateOfOrder) " +
            "FROM Order o " +
            "JOIN o.orderSizes os " +
            "JOIN os.size s " +
            "JOIN s.subProduct sp " +
            "JOIN sp.product p " +
            "JOIN sp.fbsWarehouse fw " +
            "JOIN fw.seller s2 " +
            "WHERE s2.id = :sellerId " +
            "AND os.isFbsOrder = true " +
            "AND (:keyWord IS NULL OR p.name ILIKE %:keyWord% OR sp.articulOfSeller ILIKE %:keyWord%) " +
            "AND os.orderStatus = 'PENDING' " +
            "ORDER BY o.dateOfOrder DESC ")
    Page<FBSOrdersResponse> getAllOrdersFbs(
            @Param("sellerId") Long sellerId,
            @Param("keyWord") String keyWord,
            Pageable pageable);

    @Query("""
            SELECT new com.example.baygo.db.dto.response.BuyerOrdersHistoryResponse(
              o.id, o.dateOfOrder, o.orderNumber, cast(sum(os.quantity) as int), o.totalPrice
            )
            FROM Order o
            JOIN OrderSize os ON os.order.id = o.id
            WHERE o.buyer.id = :buyerId
            AND (:keyWord IS NULL
                OR o.orderNumber iLIKE CONCAT("%", :keyWord, "%")
                OR os.size.subProduct.product.name iLIKE CONCAT("%", :keyWord, "%") )
            GROUP BY o.id, o.dateOfOrder, o.orderNumber, o.totalPrice
            ORDER BY o.dateOfOrder DESC
            """)
    List<BuyerOrdersHistoryResponse> getAllHistoryOfOrder(Long buyerId,
                                                          String keyWord);

    @Query("""
             SELECT new com.example.baygo.db.dto.response.BuyerOrderProductsResponse(
                          os.size.id, os.quantity, os.orderStatus, os.dateOfReceived, os.qrCode, os.percentOfDiscount,
                          os.price, os.size.subProduct.mainImage, os.size.subProduct.product.name, os.size.size
            )
            FROM OrderSize os
            WHERE os.order.id = :orderId
            """)
    List<BuyerOrderProductsResponse> getProductOfOrderByOrderId(Long orderId);

    @Query("""
            SELECT new com.example.baygo.db.dto.response.BuyerOrderHistoryDetailResponse(
            o.dateOfOrder, o.orderNumber, o.withDelivery, sum(os.price), cast(sum(os.price * (os.percentOfDiscount / 100.0)) as bigdecimal ), o.totalPrice
            )
            FROM Order o
            JOIN OrderSize os ON os.order.id = o.id
            WHERE o.id = :orderId
            GROUP BY o.dateOfOrder, o.orderNumber, o.withDelivery, o.totalPrice
            """)
    BuyerOrderHistoryDetailResponse getHistoryOfOrderById(Long orderId);
}