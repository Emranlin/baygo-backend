package com.example.baygo.api.auth;


import com.example.baygo.db.dto.request.*;
import com.example.baygo.db.dto.request.AuthenticateRequest;
import com.example.baygo.db.dto.request.ForgotPasswordRequest;
import com.example.baygo.db.dto.request.ResetPasswordRequest;
import com.example.baygo.db.dto.response.AuthenticationResponse;
import com.example.baygo.db.dto.response.SimpleResponse;
import com.example.baygo.service.AuthenticationService;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthenticationApi {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new buyer", description = "This method validates the request and creates a new buyer.")
    @PostMapping("/sign-up/buyer")
    public AuthenticationResponse signUpBuyer(@RequestBody @Valid BuyerRegisterRequest request) {
        return authenticationService.buyerRegister(request);
    }

    @Operation(summary = "Register a new seller", description = "This method validates the request and creates a new seller.")
    @PostMapping("/sign-up/seller")
    public AuthenticationResponse signUpSeller(@RequestBody @Valid SellerRegisterRequest request) {
        return authenticationService.sellerRegister(request);
    }

    @Operation(summary = "Authenticate a user", description = "This method validates the request and authenticates a user.")
    @PostMapping("/sign-in")
    public AuthenticationResponse signIn(@RequestBody @Valid AuthenticateRequest request) {
        return authenticationService.authenticate(request);
    }

    @Operation(summary = "Forgot password", description = "This method sends message to email for reset password.")
    @PostMapping("/forgot-password")
    public ResponseEntity<SimpleResponse> processForgotPasswordForm(@RequestBody @Valid ForgotPasswordRequest request) throws MessageDescriptorFormatException {
        return ResponseEntity.ok(authenticationService.
                forgotPassword(request.email()));
    }

    @Operation(summary = "Reset password", description = "This method changes the old password to new password.")
    @PostMapping("/reset-password")
    public ResponseEntity<SimpleResponse> resetPassword(@RequestParam String token, @RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(authenticationService.resetPassword(token, request.newPassword()));
    }

    @Operation(summary = "Google", description = "This method validates the request and authenticates a user with google.")
    @PostMapping("/google")
    public AuthenticationResponse google(@RequestParam String tokenId) throws FirebaseAuthException {
        return authenticationService.authWithGoogle(tokenId);
    }

    @Operation(summary = "Confirm", description = "This method for confirm code checking method")
    @PostMapping("/confirm")
    AuthenticationResponse confirmRegistration(String email, int code){
        return authenticationService.confirmRegistration(email, code);
    }
}
