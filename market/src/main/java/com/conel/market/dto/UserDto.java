package com.conel.market.dto;

public record UserDto(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
        ) {
}
