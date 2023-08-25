package com.example.baygo.service.impl;

import com.example.baygo.db.dto.response.HomePageResponse;
import com.example.baygo.repository.ProductRepository;
import com.example.baygo.service.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class HomePageServiceImpl implements HomePageService {
    private final ProductRepository productRepository;
    @Override
    public List<HomePageResponse> getBestsellersForHomePage() {
        return productRepository.getBestSellersForHomePage();
    }

    @Override
    public List<HomePageResponse> getHotSalesForHomePage() {
        return productRepository.getHotSalesForHomePage();
    }

    @Override
    public List<HomePageResponse> getFashionProductsForHomePage() {
        return productRepository.getFashionProductsForHomePage(PageRequest.of(0, 8));
    }
}