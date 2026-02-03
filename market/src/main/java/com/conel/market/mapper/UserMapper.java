package com.conel.market.mapper;

import com.conel.market.dto.UserDto;
import com.conel.market.dto.UserResponseDto;
import com.conel.market.models.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    //WHAT ENTITY NEEDS
    public User toUser(UserDto dto){
        return User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .password(dto.password())
                .role(dto.role())
                .build();
    }
    //WHAT USER NEEDS
    public UserResponseDto toUserResponseDto(User user){
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}
