package com.group2.policy_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.group2.policy_service.dto.PolicyRequestDTO;
import com.group2.policy_service.dto.PolicyResponseDTO;
import com.group2.policy_service.dto.UserPolicyResponseDTO;
import com.group2.policy_service.service.IPolicyCommandService;

import com.group2.policy_service.entity.PolicyStatus;

@ExtendWith(MockitoExtension.class)
public class PolicyCommandControllerTest {

    @Mock
    private IPolicyCommandService commandService;

    @InjectMocks
    private PolicyCommandController controller;

    private PolicyResponseDTO mockPolicyResponse;
    private UserPolicyResponseDTO mockUserPolicyResponse;

    @BeforeEach
    void setUp() {
        mockPolicyResponse = new PolicyResponseDTO();
        mockPolicyResponse.setId(10L);
        mockPolicyResponse.setPolicyName("Health Plan");

        mockUserPolicyResponse = new UserPolicyResponseDTO();
        mockUserPolicyResponse.setId(5L);
        mockUserPolicyResponse.setPolicyName("Health Plan");
        mockUserPolicyResponse.setStatus(PolicyStatus.ACTIVE);
    }

    @Test
    void testCreatePolicy() {
        when(commandService.createPolicy(any(PolicyRequestDTO.class))).thenReturn(mockPolicyResponse);
        PolicyResponseDTO res = controller.createPolicy(new PolicyRequestDTO());
        assertEquals(10L, res.getId());
    }

    @Test
    void testUpdatePolicy() {
        when(commandService.updatePolicy(eq(10L), any(PolicyRequestDTO.class))).thenReturn(mockPolicyResponse);
        PolicyResponseDTO res = controller.updatePolicy(10L, new PolicyRequestDTO());
        assertEquals(10L, res.getId());
    }

    @Test
    void testDeletePolicy() {
        doNothing().when(commandService).deletePolicy(10L);
        controller.deletePolicy(10L);
    }

    @Test
    void testPurchasePolicy() {
        when(commandService.purchasePolicy(10L)).thenReturn(mockUserPolicyResponse);
        UserPolicyResponseDTO res = controller.purchasePolicy(10L);
        assertEquals(5L, res.getId());
    }

    @Test
    void testRequestCancellation() {
        when(commandService.requestCancellation(eq(5L), any())).thenReturn(mockUserPolicyResponse);
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("reason", "Reason");
        ResponseEntity<UserPolicyResponseDTO> res = controller.requestCancellation(5L, body);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testApproveCancellation() {
        when(commandService.approveCancellation(5L)).thenReturn(mockUserPolicyResponse);
        ResponseEntity<UserPolicyResponseDTO> res = controller.approveCancellation(5L);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testPayPremium() {
        when(commandService.payPremium(5L, 100.0)).thenReturn(mockUserPolicyResponse);
        ResponseEntity<UserPolicyResponseDTO> res = controller.payPremium(5L, 100.0);
        assertEquals(200, res.getStatusCode().value());
    }
}
