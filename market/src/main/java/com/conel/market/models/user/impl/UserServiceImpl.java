package com.conel.market.models.user.impl;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.models.user.User;
import com.conel.market.models.user.UserMapper;
import com.conel.market.models.user.UserRepository;
import com.conel.market.models.user.UserService;
import com.conel.market.models.user.request.ChangePasswordRequest;
import com.conel.market.models.user.request.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.conel.market.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(()->new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public void updateProfileInfo(final ProfileUpdateRequest request, final String userId) {
        final User savedUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        this.userMapper.mergeUserInfo(savedUser, request);
        this.userRepository.save(savedUser);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, final String userId) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())){
            throw new BusinessException(CHANGE_PASSWORD_MISMATCH);
        }

        final User savedUser=this.userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(USER_NOT_FOUND));

        if (!this.passwordEncoder.matches(request.getCurrentPassword(),savedUser.getPassword())){
            throw new BusinessException(INVALID_CURRENT_PASSWORD);
        }

        final String encoded=this.passwordEncoder.encode(request.getNewPassword());
        savedUser.setPassword(encoded);
        this.userRepository.save(savedUser);
    }

    @Override
    public void deactivateAccount(String userId) {
        final User user=this.userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(USER_NOT_FOUND));

        if (!user.isEnabled()){
            throw new BusinessException(ACCOUNT_ALREADY_DEACTIVATED);
        }
        user.setEnabled(false);
        this.userRepository.save(user);
    }

    @Override
    public void reactivateAccount(String userId) {
        final User user=this.userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(USER_NOT_FOUND));

        if (user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_ACTIVATED);
        }
        user.setEnabled(true);
        this.userRepository.save(user);
    }

    @Override
    public void deleteAccount(String userId) {

    }
}
