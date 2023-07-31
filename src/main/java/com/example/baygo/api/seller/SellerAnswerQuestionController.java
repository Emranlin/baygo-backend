package com.example.baygo.api.seller;

import com.example.baygo.db.dto.request.AnswerOfSellerRequest;
import com.example.baygo.db.dto.response.BuyerQuestionResponse;
import com.example.baygo.db.dto.response.PaginationResponse;
import com.example.baygo.db.dto.response.SimpleResponse;
import com.example.baygo.service.AnswerOfSellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/answer")
@RequiredArgsConstructor
@Tag(name = "Seller answer question")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SellerAnswerQuestionController {
    private final AnswerOfSellerService answerOfSellerService;

    @Operation(summary = "Save answer!", description = "This method saves answer for question!")
    @PostMapping
    @PreAuthorize("hasAuthority('SELLER')")
    public SimpleResponse saveAnswer(@Valid @RequestBody AnswerOfSellerRequest request) {
        return answerOfSellerService.addAnswer(request);
    }

    @Operation(summary = "Get all questions!", description = "This method gets all questions!")
    @PreAuthorize("hasAuthority('SELLER')")
    @GetMapping("/questions")
    public PaginationResponse<BuyerQuestionResponse> getQuestions(@RequestParam(required = false) String keyWord,
                                                                  @RequestParam(required = false, defaultValue = "1") int page,
                                                                  @RequestParam(required = false, defaultValue = "3") int pageSize) {
        return answerOfSellerService.getAllQuestions(keyWord, page, pageSize);
    }
}