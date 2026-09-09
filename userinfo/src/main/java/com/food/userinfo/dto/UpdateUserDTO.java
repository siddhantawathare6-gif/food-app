package com.food.userinfo.dto;

import com.food.userinfo.validation.OptionalPattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @OptionalPattern(regexp = "^[0-9]{10}$", message = "Alternate mobile number must be 10 digits")
    private String alternateMobileNumber;

    @Valid
    private AddressDTO address;
}
