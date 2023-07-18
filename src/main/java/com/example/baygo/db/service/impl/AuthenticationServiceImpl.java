package com.example.baygo.db.service.impl;

import com.example.baygo.db.config.jwt.JwtService;
import com.example.baygo.db.dto.request.auth.AuthenticateRequest;
import com.example.baygo.db.dto.request.BuyerRegisterRequest;
import com.example.baygo.db.dto.request.SellerRegisterRequest;
import com.example.baygo.db.dto.response.AuthenticationResponse;
import com.example.baygo.db.dto.response.SimpleResponse;
import com.example.baygo.db.exceptions.AlreadyExistException;
import com.example.baygo.db.exceptions.BadCredentialException;
import com.example.baygo.db.exceptions.BadRequestException;
import com.example.baygo.db.exceptions.NotFoundException;
import com.example.baygo.db.model.Buyer;
import com.example.baygo.db.model.Seller;
import com.example.baygo.db.model.User;
import com.example.baygo.db.model.enums.Gender;
import com.example.baygo.db.model.enums.Role;
import com.example.baygo.db.repository.SellerRepository;
import com.example.baygo.db.repository.UserRepository;
import com.example.baygo.db.service.AuthenticationService;
import com.example.baygo.db.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userInfoRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final SellerRepository sellerRepository;


    @Override
    public AuthenticationResponse buyerRegister(BuyerRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.error(String.format("Пользователь с адресом электронной почты %s уже существует", request.email()));
            throw new AlreadyExistException(String.format("Пользователь с адресом электронной почты %s уже существует", request.email()));
        }
        String split = request.email().split("@")[0];
        if (split.equals(request.password())) {
            throw new BadRequestException("Создайте более надежный пароль");
        }
        Buyer buyer = Buyer.builder()
                .address(request
                        .address())
                .dateOfBirth(request
                        .dateOfBirth())
                .gender(Gender
                        .valueOf(request
                                .gender())).build();
        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.BUYER)
                .buyer(buyer)
                .build();

        userRepository.save(user);
        log.info(String.format("Пользователь %s успешно сохранен!", user.getEmail()));
        String token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    @Override
    public AuthenticationResponse sellerRegister(SellerRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.error(String.format("Продавец с адресом электронной почты %s уже существует", request.email()));
            throw new AlreadyExistException(String.format("Продавец с адресом электронной почты %s уже существует", request.email()));
        }
        String split = request.email().split("@")[0];
        if (split.equals(request.password())) {
            throw new BadRequestException("Создайте более надежный пароль");
        }
        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.SELLER)
                .build();

        Seller seller = Seller.builder()
                .ITN(request.ITN())
                .address(request.address())
                .BIC(request.BIC())
                .nameOfStore(request.nameOfStore())
                .vendorNumber(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .user(user)
                .build();

        sellerRepository.save(seller);
        log.info(String.format("Пользователь %s успешно сохранен!", user.getEmail()));
        String token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticateRequest request) {

        User userInfo = userInfoRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error(String.format("Пользователь с адресом электронной почты %s не существует", request.email()));
                    throw new BadCredentialException(String.format("Пользователь с адресом электронной почты %s не существует", request.email()));
                });
        if (!passwordEncoder.matches(request.password(), userInfo.getPassword())) {
            log.error("Пароль не подходит");
            throw new BadRequestException("Пароль не подходит");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        log.info(String.format("Пользователь %s успешно аутентифицирован", userInfo.getEmail()));
        String token = jwtService.generateToken(userInfo);

        return AuthenticationResponse.builder()
                .email(userInfo.getEmail())
                .role(userInfo.getRole())
                .token(token)
                .build();
    }

    @Override
    public SimpleResponse forgotPassword(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.error(String.format("Пользователь с адресом электронной почты %s не существует", email));
                    throw new NotFoundException(String.format("Пользователь с адресом электронной почты %s не существует", email));
                });
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);

        String subject = "Запрос на сброс пароля";
        String resetPasswordLink = "http://localhost:2023/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("title", "Восстановление пароля");
        context.setVariable("message", "Пожалуйста, нажмите на ссылку ниже для сброса пароля!");
        context.setVariable("token", resetPasswordLink);
        context.setVariable("tokenTitle", "Восстановление пароля");

        String htmlContent = templateEngine.process("reset-password.html", context);

        emailService.sendEmail(email, subject, htmlContent);
        return SimpleResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("Сброс пароля был отправлен на вашу электронную почту. Пожалуйста, проверьте свою электронную почту.")
                .build();
    }

    @Override
    public SimpleResponse resetPassword(String token, String newPassword) {
        User user = userInfoRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> {
                    log.error("Пользователь не существует");
                    throw new NotFoundException("Пользователь не существует");});

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        log.info("Пароль пользователя успешно изменен!");
        return SimpleResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("Пароль пользователя успешно изменен!")
                .build();
    }
}
