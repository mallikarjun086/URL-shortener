package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenUrlRequest {

    @NotBlank(message = "Original URL cannot be blank")
    @URL(message = "Invalid URL format")
    private String longUrl;

    private String customAlias;

    private Integer expiresInDays;
}
