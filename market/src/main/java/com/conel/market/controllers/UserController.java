package com.conel.market.controllers;

import com.conel.market.dto.UserDto;
import com.conel.market.dto.UserResponseDto;
import com.conel.market.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserDto user){

        UserResponseDto response= userService.saveUser(user);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto > findById(@PathVariable Integer id){

        UserResponseDto responseDto= userService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll(){

        List<UserResponseDto> response= userService.findAll();
        return ResponseEntity.ok(response);
    }
}
