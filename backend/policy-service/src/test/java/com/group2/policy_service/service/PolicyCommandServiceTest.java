package com.group2.policy_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.group2.policy_service.dto.PolicyRequestDTO;
import com.group2.policy_service.dto.PolicyResponseDTO;
import com.group2.policy_service.dto.UserPolicyResponseDTO;
import com.group2.policy_service.entity.Policy;
import com.group2.policy_service.entity.PolicyStatus;
import com.group2.policy_service.entity.PolicyType;
import com.group2.policy_service.entity.UserPolicy;
import com.group2.policy_service.feign.AuthClient;
import com.group2.policy_service.feign.UserDTO;
import com.group2.policy_service.repository.PolicyRepository;
import com.group2.policy_service.repository.PolicyTypeRepository;
import com.group2.policy_service.repository.UserPolicyRepository;
import com.group2.policy_service.service.impl.AsyncNotificationService;
import com.group2.policy_service.service.impl.PolicyCommandServiceImpl;
import com.group2.policy_service.util.PolicyMapper;

@ExtendWith(MockitoExtension.class)
public class PolicyCommandServiceTest {

    @Mock private PolicyRepository policyRepository;
    @Mock private UserPolicyRepository userPolicyRepository;
    @Mock private PolicyTypeRepository policyTypeRepository;
    @Mock private PolicyMapper mapper;
    @Mock private AuthClient authClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private AsyncNotificationService asyncNotificationService;

    @InjectMocks
    private PolicyCommandServiceImpl policyCommandService;

    private Policy mockPolicy;
    private UserPolicy mockUserPolicy;
    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        mockPolicy = new Policy();
        mockPolicy.setId(10L);
        mockPolicy.setPolicyName("Health Plan");
        mockPolicy.setDurationInMonths(12);
        mockPolicy.setPremiumAmount(100.0);
        mockPolicy.setCoverageAmount(50000.0);
        mockPolicy.setActive(true);

        mockUserPolicy = new UserPolicy();
        mockUserPolicy.setId(5L);
        mockUserPolicy.setPolicy(mockPolicy);
        mockUserPolicy.setUserId(100L);
        mockUserPolicy.setStatus(PolicyStatus.ACTIVE);
        mockUserPolicy.setOutstandingBalance(0.0);

        mockUser = new UserDTO();
        mockUser.setId(100L);
        mockUser.setName("Test User");
        mockUser.setEmail("test@test.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setUpSecurityContext(Long userId) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void testPurchasePolicy_Success() {
        setUpSecurityContext(100L);

        when(policyRepository.findById(10L)).thenReturn(Optional.of(mockPolicy));
        when(userPolicyRepository.findByUserId(100L)).thenReturn(Collections.emptyList());
        when(userPolicyRepository.save(any(UserPolicy.class))).thenReturn(mockUserPolicy);
        when(mapper.mapToUserPolicyResponse(any(UserPolicy.class))).thenReturn(new UserPolicyResponseDTO());
        when(authClient.getUserById(100L)).thenReturn(mockUser);

        UserPolicyResponseDTO response = policyCommandService.purchasePolicy(10L);

        assertNotNull(response);
        verify(userPolicyRepository, times(1)).save(any(UserPolicy.class));
    }

    @Test
    void testPurchasePolicy_AlreadyHasActive() {
        setUpSecurityContext(100L);

        UserPolicy existing = new UserPolicy();
        existing.setPolicy(mockPolicy);
        existing.setStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findById(10L)).thenReturn(Optional.of(mockPolicy));
        when(userPolicyRepository.findByUserId(100L)).thenReturn(Arrays.asList(existing));

        assertThrows(RuntimeException.class, () -> policyCommandService.purchasePolicy(10L));
    }

    @Test
    void testRequestCancellation_Success() {
        setUpSecurityContext(100L);

        when(userPolicyRepository.findById(5L)).thenReturn(Optional.of(mockUserPolicy));
        when(mapper.mapToUserPolicyResponse(any(UserPolicy.class))).thenReturn(new UserPolicyResponseDTO());
        when(authClient.getUserById(100L)).thenReturn(mockUser);

        UserPolicyResponseDTO response = policyCommandService.requestCancellation(5L, "Reason");

        assertNotNull(response);
        assertEquals(PolicyStatus.PENDING_CANCELLATION, mockUserPolicy.getStatus());
        verify(userPolicyRepository, times(1)).save(mockUserPolicy);
    }

    @Test
    void testApproveCancellation_Success() {
        mockUserPolicy.setStatus(PolicyStatus.PENDING_CANCELLATION);
        mockUserPolicy.setOutstandingBalance(0.0);
        when(userPolicyRepository.findById(5L)).thenReturn(Optional.of(mockUserPolicy));
        when(mapper.mapToUserPolicyResponse(any())).thenReturn(new UserPolicyResponseDTO());
        when(authClient.getUserById(any())).thenReturn(mockUser);

        UserPolicyResponseDTO response = policyCommandService.approveCancellation(5L);

        assertNotNull(response);
        assertEquals(PolicyStatus.CANCELLED, mockUserPolicy.getStatus());
    }

    @Test
    void testCreatePolicy_Success() {
        PolicyRequestDTO dto = new PolicyRequestDTO();
        dto.setPolicyTypeId(1L);
        dto.setPolicyName("New Plan");

        when(policyTypeRepository.findById(1L)).thenReturn(Optional.of(new PolicyType()));
        when(policyRepository.save(any(Policy.class))).thenReturn(mockPolicy);
        when(mapper.mapToPolicyResponse(any(Policy.class))).thenReturn(new PolicyResponseDTO());

        PolicyResponseDTO response = policyCommandService.createPolicy(dto);

        assertNotNull(response);
    }

    @Test
    void testUpdatePolicy_Success() {
        PolicyRequestDTO dto = new PolicyRequestDTO();
        dto.setPolicyName("Updated Plan");

        when(policyRepository.findById(10L)).thenReturn(Optional.of(mockPolicy));
        when(mapper.mapToPolicyResponse(any())).thenReturn(new PolicyResponseDTO());

        PolicyResponseDTO response = policyCommandService.updatePolicy(10L, dto);

        assertNotNull(response);
    }

    @Test
    void testDeletePolicy_Success() {
        when(policyRepository.findById(10L)).thenReturn(Optional.of(mockPolicy));
        policyCommandService.deletePolicy(10L);
        assertFalse(mockPolicy.isActive());
    }

    @Test
    void testPayPremium_Success() {
        mockUserPolicy.setOutstandingBalance(500.0);
        when(userPolicyRepository.findById(5L)).thenReturn(Optional.of(mockUserPolicy));
        when(mapper.mapToUserPolicyResponse(any())).thenReturn(new UserPolicyResponseDTO());
        when(authClient.getUserById(any())).thenReturn(mockUser);

        UserPolicyResponseDTO response = policyCommandService.payPremium(5L, 200.0);

        assertNotNull(response);
        assertEquals(300.0, mockUserPolicy.getOutstandingBalance());
    }
}
