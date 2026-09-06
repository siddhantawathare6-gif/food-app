package com.food.userinfo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    @NotBlank(message = "EmailOrUsername required")
    private String emailOrUsername;
    @NotBlank(message = "Password is required")
    private String password;
}
