package com.conel.market.controllers;

import com.conel.market.dto.UserDto;
import com.conel.market.dto.UserResponseDto;
import com.conel.market.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponseDto create(@RequestBody UserDto user){
        return userService.saveUser(user);
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Integer id){
        return userService.findById(id);
    }

    @GetMapping
    public List<UserResponseDto> findAll(){
        return userService.findAll();
    }
}
