package com.conel.market.services;

import com.conel.market.dto.UserDto;
import com.conel.market.dto.UserResponseDto;
import com.conel.market.mapper.UserMapper;
import com.conel.market.models.User;
import com.conel.market.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User getUserEntity(Integer id){
        return userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("User not found with id: " + id));
    }

    public UserResponseDto saveUser(UserDto dto){
        if (userRepository.existsByEmail(dto.email())){
            throw new RuntimeException("This email is already registered!");
        }
        User user=userMapper.toUser(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //save
        User savedUser=userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }
    public List<UserResponseDto> findAll(){
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }

    public UserResponseDto findById(Integer id){
        User user=getUserEntity(id);
        return userMapper.toUserResponseDto(user);
    }
}

