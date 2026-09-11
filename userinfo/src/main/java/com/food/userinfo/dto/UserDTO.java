package com.food.userinfo.dto;

import com.food.userinfo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String name;
    private String username;
    private String email;
    private String password;
    private String mobileNumber;
    private String alternateMobileNumber;
    private Set<Role> roles;

    private AddressDTO address;
    private String imageUrl;

}
