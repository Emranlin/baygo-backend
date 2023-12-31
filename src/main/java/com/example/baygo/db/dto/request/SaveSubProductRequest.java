package com.example.baygo.db.dto.request;

import com.example.baygo.validations.ImageUrlValid;
import com.example.baygo.validations.ImageUrlsValid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record SaveSubProductRequest(
        @NotBlank(message = "Код цвета должен быть указан!!!")
        String colorHexCode,
        @NotBlank(message = "Цвет должен быть указан!!!")
        String color,
        @NotNull(message = "Цена должна быть указана!!!")
        @Min(value = 0, message = "Цена должна быть положительным числом!!")
        BigDecimal price,
        @NotEmpty(message = "Изображение должно быть указано!!!")
        @ImageUrlValid
        String mainImage,
        @NotEmpty(message = "Изображения должны быть указаны!!!")
        @ImageUrlsValid
        List<String> images,
        @NotBlank(message = "Характеристика должна быть указана!!!")
        String description,
        @NotBlank(message = "Артикул продавца должен быть указан!!!")
        String articulOfSeller,
        @Positive(message = "Высота должен быть  положительным !")
        int height,
        @Positive(message = "Ширина должна быть положительным! ")
        int width,
        @Positive(message = "Длина должна быть положительным !")
        int length,
        @Positive(message = "Вес должен быть положительным !")
        double weight,
        @Valid
        @NotEmpty(message = "Размеры не должны быть пустыми!!!")
        List<SaveSizeRequest>sizes
) {
}
