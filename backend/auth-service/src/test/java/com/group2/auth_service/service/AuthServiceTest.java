package com.group2.auth_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.group2.auth_service.dto.AuthResponse;
import com.group2.auth_service.dto.LoginRequest;
import com.group2.auth_service.dto.RegisterRequest;
import com.group2.auth_service.dto.UserResponseDTO;
import com.group2.auth_service.entity.Role;
import com.group2.auth_service.entity.User;
import com.group2.auth_service.exception.UserAlreadyExistsException;
import com.group2.auth_service.feign.NotificationClient;
import com.group2.auth_service.repository.AuthServiceRepository;
import com.group2.auth_service.security.JwtUtil;
import com.group2.auth_service.service.impl.AuthServiceImpl;
import com.group2.auth_service.util.AuthMapper;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private AuthServiceRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private NotificationClient notificationClient;
    @Mock private AuthMapper authMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    
    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@test.com");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setRole(Role.CUSTOMER);
        sampleUser.setRefreshToken("mockOldRefresh");
    }

    @Test
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("password");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(notificationClient.isOtpVerified("new@test.com")).thenReturn(ResponseEntity.ok(true));
        when(authMapper.mapToUser(any())).thenReturn(sampleUser);
        when(authMapper.mapToResponse(any())).thenReturn(new UserResponseDTO());

        assertNotNull(authService.register(req));
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testRegister_AlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(sampleUser));
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(req));
    }

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("pass", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("mockAccess");
        when(jwtUtil.generateRefreshToken(anyString(), anyLong())).thenReturn("mockRefresh");

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("mockAccess", res.getToken());
        assertEquals("mockRefresh", res.getRefreshToken());
    }

    @Test
    void testRefreshToken_Success() {
        when(jwtUtil.validateToken("mockOldRefresh")).thenReturn(true);
        when(jwtUtil.extractUserId("mockOldRefresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("newAccess");
        when(jwtUtil.generateRefreshToken(anyString(), anyLong())).thenReturn("newRefresh");

        AuthResponse res = authService.refreshToken("mockOldRefresh");

        assertNotNull(res);
        assertEquals("newAccess", res.getToken());
        assertEquals("newRefresh", res.getRefreshToken());
    }

    @Test
    void testGetProfile_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(1L);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(authMapper.mapToResponse(any())).thenReturn(new UserResponseDTO());

        assertNotNull(authService.getProfile());
        SecurityContextHolder.clearContext();
    }
}
