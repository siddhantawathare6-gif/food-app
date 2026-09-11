package com.food.userinfo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Column(nullable = false, unique = true, updatable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true, updatable = false)
    private String email;
    @Column(nullable = false, unique = true, updatable = false)
    private String mobileNumber;
    @Column(unique = true)
    private String alternateMobileNumber;

    @Embedded
    private Address address;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_roles",  // Join table name
            joinColumns = @JoinColumn(name = "user_id"),  // Foreign key to User
            inverseJoinColumns = @JoinColumn(name = "role_id")  // Foreign key to Role
    )
    private Set<Role> roles;
    private String imageUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
