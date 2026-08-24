package com.food.userinfo.dto;

import com.food.userinfo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {

    private String name;
    private String username;
    private String email;
    private String password;
    private Set<Role> roles;
}
