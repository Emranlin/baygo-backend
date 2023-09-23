package com.example.baygo.service.impl;

import com.example.baygo.config.jwt.JwtService;
import com.example.baygo.db.dto.request.order.BuyerOrderRequest;
import com.example.baygo.db.dto.request.order.ProductOrderRequest;
import com.example.baygo.db.dto.response.*;
import com.example.baygo.db.dto.response.orders.AnalysisResponse;
import com.example.baygo.db.dto.response.orders.FBBOrderResponse;
import com.example.baygo.db.dto.response.orders.OrderWareHouseResponse;
import com.example.baygo.db.dto.response.orders.RecentOrdersResponse;
import com.example.baygo.db.exceptions.BadRequestException;
import com.example.baygo.db.exceptions.MessageSendingException;
import com.example.baygo.db.exceptions.NotFoundException;
import com.example.baygo.db.model.*;
import com.example.baygo.db.model.enums.OrderStatus;
import com.example.baygo.db.model.enums.PaymentType;
import com.example.baygo.repository.OrderRepository;
import com.example.baygo.repository.OrderSizeRepository;
import com.example.baygo.repository.SizeRepository;
import com.example.baygo.repository.custom.CustomOrderRepository;
import com.example.baygo.service.BarcodeService;
import com.example.baygo.service.OrderService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final CustomOrderRepository customOrderRepository;
    private final JwtService jwtService;
    private final OrderRepository orderRepository;
    private final OrderSizeRepository orderSizeRepository;
    private final SizeRepository sizeRepository;
    private final JavaMailSender javaMailSender;
    private final Configuration configuration;
    private final BarcodeService barcodeService;

    @Override
    public SimpleResponse saveBuyerOrder(BuyerOrderRequest buyerOrderRequest) {
        Buyer buyer = jwtService.getAuthenticate().getBuyer();
        Order order = new Order();
        List<OrderSize> orderSizeList = new ArrayList<>();

        for (ProductOrderRequest productOrderRequest : buyerOrderRequest.productOrderRequests()) {
            Size size = sizeRepository.findById(productOrderRequest.sizeId()).orElseThrow(
                    () -> new NotFoundException(String.format("Продукт идентификатором %s не найден.", productOrderRequest.sizeId())));

            OrderSize orderSize = new OrderSize();
            orderSize.setOrder(order);
            orderSize.setOrderStatus(OrderStatus.PENDING);
            orderSize.setPercentOfDiscount(productOrderRequest.percentOfDiscount());
            BigDecimal price = size.getSubProduct().getPrice().multiply(new BigDecimal(productOrderRequest.quantityProduct()));
            orderSize.setPrice(price);
            orderSize.setSize(size);

            if (sizeRepository.isFbb(size.getId()) && size.getFbbQuantity() > 0) {
                if (size.getFbbQuantity() >= productOrderRequest.quantityProduct()) {
                    orderSize.setFbbOrder(true);
                    orderSize.setFbsOrder(false);
                    orderSize.setFbbQuantity(productOrderRequest.quantityProduct());

                    size.setFbbQuantity(size.getFbbQuantity() - productOrderRequest.quantityProduct());
                } else {
                    int fbsQuantity = productOrderRequest.quantityProduct() - size.getFbbQuantity();
                    orderSize.setFbbOrder(true);
                    orderSize.setFbsOrder(true);
                    orderSize.setFbbQuantity(size.getFbbQuantity());
                    orderSize.setFbsQuantity(fbsQuantity);

                    size.setFbbQuantity(0);
                    size.setFbsQuantity(size.getFbsQuantity() - fbsQuantity);
                }
            } else {
                orderSize.setFbbOrder(false);
                orderSize.setFbsOrder(true);
                orderSize.setFbsQuantity(productOrderRequest.quantityProduct());

                size.setFbsQuantity(size.getFbsQuantity() - productOrderRequest.quantityProduct());
            }

            QRCodeWithImageResponse qrCodeWithImageResponse = barcodeService.generateQrCode();
            orderSize.setQrCode(qrCodeWithImageResponse.qrCode());
            orderSize.setQrCodeImage(qrCodeWithImageResponse.qrCodeImage());
            orderSizeRepository.save(orderSize);
            orderSizeList.add(orderSize);

            sizeRepository.save(size);
        }

        Customer customer = Customer.builder()
                .firstName(buyerOrderRequest.customerInfoRequest().firsName())
                .lastName(buyerOrderRequest.customerInfoRequest().lastName())
                .email(buyerOrderRequest.customerInfoRequest().email())
                .phoneNumber(buyerOrderRequest.customerInfoRequest().phoneNumber())
                .country(buyerOrderRequest.customerInfoRequest().country())
                .city(buyerOrderRequest.customerInfoRequest().city())
                .postalCode(buyerOrderRequest.customerInfoRequest().postalCode())
                .address(buyerOrderRequest.customerInfoRequest().address()).build();

        UUID uuid = UUID.randomUUID();
        long positiveValue = Math.abs(uuid.getLeastSignificantBits()) % 100000000;
        String orderNumber = String.format("%08d", positiveValue);

        order.setDateOfOrder(LocalDateTime.now());
        order.setTotalPrice(buyerOrderRequest.totalPrice());
        order.setWithDelivery(buyerOrderRequest.withDelivery());
        order.setBuyer(buyer);
        order.setOrderSizes(orderSizeList);
        order.setCustomer(customer);
        order.setOrderNumber(orderNumber);
        order.setPaymentType(PaymentType.ONLINE_BY_CARD);
        orderRepository.save(order);


        Map<String, Object> model = new HashMap<>();
        model.put("orderNumber", order.getOrderNumber());
        model.put("dateOfOrder", order.getDateOfOrder().toLocalDate());
        model.put("statusOfOrder", "В ожидании");
        model.put("datePurchase", order.getDateOfOrder().toLocalDate());
        model.put("customer", order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        model.put("phoneNumber", order.getCustomer().getPhoneNumber());
        String withDelivery = "Самовывоз из магазина";
        if (buyerOrderRequest.withDelivery()) {
            withDelivery = "Доставка курьером";
        }
        model.put("withDelivery", withDelivery);
        model.put("link", "https://www.youtube.com/watch?v=IhZrMyuE6EQ");
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            Template template = configuration.getTemplate("order-email.html");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            mimeMessageHelper.setTo(order.getCustomer().getEmail());
            mimeMessageHelper.setText(html, true);
            mimeMessageHelper.setSubject("BuyGo");
            mimeMessageHelper.setFrom("baygo.kgz@gmail.com");
            javaMailSender.send(message);
        } catch (MessagingException | IOException | TemplateException e) {
            throw new MessageSendingException("Ошибка при отправке сообщения!");
        }

        return SimpleResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message(String.format("Номер заказа  %s  Подтвержден", orderNumber)).build();
    }


    @Override
    public AnalysisResponse getWeeklyAnalysis(Date startDate, Date endDate, Long warehouseId, String nameOfTime) {
        User user = jwtService.getAuthenticate();
        Seller seller = user.getSeller();
        return customOrderRepository.getWeeklyAnalysis(startDate, endDate, warehouseId, nameOfTime, seller.getId());
    }

    @Override
    public List<OrderWareHouseResponse> getAllOrdersByWareHouse() {
        Long sellerId = jwtService.getAuthenticate().getSeller().getId();
        return customOrderRepository.getAllOrders(sellerId);
    }

    @Override
    public List<RecentOrdersResponse> getResentOrders() {
        Long sellerId = jwtService.getAuthenticate().getSeller().getId();
        return orderRepository.getAllRecentOrders(sellerId);
    }

    @Override
    public PaginationResponse<FBBOrderResponse> getAllOrdersByFilter(int page, int size, String keyword, OrderStatus orderStatus,Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "dateOfOrder"));
        Long sellerId = jwtService.getAuthenticate().getSeller().getId();
        Page<FBBOrderResponse> orderResponses = orderRepository.getAllOrders(sellerId, keyword, orderStatus,categoryId ,pageable);
        return new PaginationResponse<>(orderResponses.getContent(),
                orderResponses.getNumber() + 1,
                orderResponses.getTotalPages());
    }

    @Override
    public List<BuyerOrdersHistoryResponse> getAllHistoryOfOrder(String keyWord) {
        Buyer buyer = jwtService.getAuthenticate().getBuyer();
        return orderRepository.getAllHistoryOfOrder(buyer.getId(), keyWord);
    }

    @Override
    public BuyerOrderHistoryDetailResponse getOrderById(Long orderId) {
        Buyer buyer = jwtService.getAuthenticate().getBuyer();
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Заказ с идентификатором: " + orderId + " не найден!"));
        if (!buyer.getOrders().contains(order)) {
            throw new BadRequestException("Заказ с идентификатором: " + orderId + " не пренадлежит вам!");
        }
        BuyerOrderHistoryDetailResponse orderHistory = orderRepository.getHistoryOfOrderById(orderId);
        orderHistory.addToProduct(orderRepository.getProductOfOrderByOrderId(orderId));
        return orderHistory;
    }
}