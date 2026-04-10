package com.group2.admin_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

import com.group2.admin_service.dto.ClaimDTO;
import com.group2.admin_service.dto.ClaimStatusDTO;
import com.group2.admin_service.dto.ClaimStatusUpdateDTO;
import com.group2.admin_service.dto.EmailRequest;
import com.group2.admin_service.dto.NotificationEvent;
import com.group2.admin_service.dto.PolicyDTO;
import com.group2.admin_service.dto.PolicyRequestDTO;
import com.group2.admin_service.dto.PolicyStatsDTO;
import com.group2.admin_service.dto.ReportResponse;
import com.group2.admin_service.dto.ReviewRequest;
import com.group2.admin_service.dto.UserDTO;
import com.group2.admin_service.dto.UserPolicyDTO;
import com.group2.admin_service.feign.AuthFeignClient;
import com.group2.admin_service.feign.ClaimsFeignClient;
import com.group2.admin_service.feign.NotificationFeignClient;
import com.group2.admin_service.feign.PolicyFeignClient;
import com.group2.admin_service.service.impl.AdminServiceImpl;
import com.group2.admin_service.util.AdminMapper;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock private ClaimsFeignClient claimsFeignClient;
    @Mock private PolicyFeignClient policyFeignClient;
    @Mock private AuthFeignClient authFeignClient;
    @Mock private NotificationFeignClient notificationFeignClient;
    @Mock private AdminMapper adminMapper;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void testReviewClaim_Success() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus("APPROVED");
        request.setRemark("All good");

        ClaimDTO claim = new ClaimDTO();
        claim.setUserId(10L);
        claim.setPolicyId(5L);

        UserDTO user = new UserDTO();
        user.setEmail("test@test.com");
        user.setName("Test User");

        UserPolicyDTO policy = new UserPolicyDTO();
        policy.setPolicyName("Health Plan");

        when(claimsFeignClient.getClaimById(1L)).thenReturn(claim);
        when(authFeignClient.getUserById(10L)).thenReturn(user);
        when(policyFeignClient.getUserPolicyById(5L)).thenReturn(policy);

        // Make Feign email succeed
        when(notificationFeignClient.sendEmail(any(EmailRequest.class))).thenReturn(ResponseEntity.ok("OK"));

        adminService.reviewClaim(1L, request);

        verify(claimsFeignClient, times(1)).updateClaimStatus(eq(1L), any(ClaimStatusUpdateDTO.class));
        verify(notificationFeignClient, times(1)).sendEmail(any(EmailRequest.class));
        verify(rabbitTemplate, times(0)).convertAndSend(any(String.class), any(String.class), any(NotificationEvent.class));
    }

    @Test
    void testReviewClaim_FallbackToRabbit() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus("APPROVED");

        ClaimDTO claim = new ClaimDTO();
        claim.setUserId(10L);

        UserDTO user = new UserDTO();
        user.setEmail("test@test.com");

        when(claimsFeignClient.getClaimById(1L)).thenReturn(claim);
        when(authFeignClient.getUserById(10L)).thenReturn(user);
        
        // Throw exception to trigger fallback
        doThrow(new RuntimeException("Feign Failure")).when(notificationFeignClient).sendEmail(any(EmailRequest.class));

        adminService.reviewClaim(1L, request);

        verify(claimsFeignClient, times(1)).updateClaimStatus(eq(1L), any(ClaimStatusUpdateDTO.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("notification.exchange"), eq("notification.send"), any(NotificationEvent.class));
    }

    @Test
    void testGetClaimStatus() {
        when(claimsFeignClient.getClaimStatus(1L)).thenReturn(new ClaimDTO());
        assertNotNull(adminService.getClaimStatus(1L));
    }

    @Test
    void testGetClaimsByUserId() {
        when(claimsFeignClient.getClaimsByUserId(1L)).thenReturn(Arrays.asList(new ClaimDTO()));
        assertEquals(1, adminService.getClaimsByUserId(1L).size());
    }

    @Test
    void testCreatePolicy() {
        when(policyFeignClient.createPolicy(any(PolicyRequestDTO.class))).thenReturn(new PolicyDTO());
        assertNotNull(adminService.createPolicy(new PolicyRequestDTO()));
    }

    @Test
    void testUpdatePolicy() {
        when(policyFeignClient.updatePolicy(eq(1L), any(PolicyRequestDTO.class))).thenReturn(new PolicyDTO());
        assertNotNull(adminService.updatePolicy(1L, new PolicyRequestDTO()));
    }

    @Test
    void testDeletePolicy() {
        adminService.deletePolicy(1L);
        verify(policyFeignClient, times(1)).deletePolicy(1L);
    }

    @Test
    void testGetReports() {
        when(claimsFeignClient.getClaimStats()).thenReturn(new ClaimStatusDTO());
        when(policyFeignClient.getPolicyStats()).thenReturn(new PolicyStatsDTO());
        when(adminMapper.mapToReportResponse(any(), any())).thenReturn(new ReportResponse());
        
        assertNotNull(adminService.getReports());
    }
}
