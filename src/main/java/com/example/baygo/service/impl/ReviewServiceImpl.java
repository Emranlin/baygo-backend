package com.example.baygo.service.impl;

import com.example.baygo.config.jwt.JwtService;
import com.example.baygo.db.dto.response.GetAllReviewsResponse;
import com.example.baygo.db.dto.response.PaginationReviewAndQuestionResponse;
import com.example.baygo.db.dto.response.ReviewResponse;
import com.example.baygo.repository.ReviewRepository;
import com.example.baygo.repository.custom.CustomReviewRepository;
import com.example.baygo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final CustomReviewRepository customReviewRepository;
    private final JwtService jwtService;
    private final ReviewRepository reviewRepository;

    @Override
    public PaginationReviewAndQuestionResponse<ReviewResponse> getAllReviews(String keyword, boolean isAnswered, int page, int size) {
        Long sellerId = jwtService.getAuthenticate().getSeller().getId();
        PaginationReviewAndQuestionResponse<ReviewResponse> allReviews = customReviewRepository.getAllReviews(sellerId, keyword, isAnswered, page, size);
        allReviews.setCountOfUnanswered(reviewRepository.countOfUnansweredBySellerId(sellerId));
        allReviews.setCountOfArchive(reviewRepository.countOfAnsweredBySellerId(sellerId));
        return allReviews;
    }

    @Override
    public List<GetAllReviewsResponse> getAllReviewsForLandingOfSeller() {
        Long sellerId = jwtService.getAuthenticate().getSeller().getId();
        return customReviewRepository.getAllReviewsForSeller(sellerId);
    }
}
