package com.group2.claims_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

import com.group2.claims_service.dto.ClaimRequestDTO;
import com.group2.claims_service.dto.ClaimResponseDTO;
import com.group2.claims_service.entity.Claim;
import com.group2.claims_service.entity.ClaimStatus;
import com.group2.claims_service.feign.AuthClient;
import com.group2.claims_service.feign.NotificationClient;
import com.group2.claims_service.feign.PolicyClient;
import com.group2.claims_service.feign.UserDTO;
import com.group2.claims_service.feign.UserPolicyDTO;
import com.group2.claims_service.repository.ClaimDocumentRepository;
import com.group2.claims_service.repository.ClaimRepository;
import com.group2.claims_service.service.impl.ClaimServiceImpl;
import com.group2.claims_service.util.ClaimMapper;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private ClaimDocumentRepository documentRepository;
    @Mock private AuthClient authClient;
    @Mock private PolicyClient policyClient;
    @Mock private NotificationClient notificationClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ClaimMapper claimMapper;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private Claim claim;

    @BeforeEach
    void setUp() {
        claim = new Claim();
        claim.setId(10L);
        claim.setPolicyId(5L);
        claim.setUserId(2L);
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
    }

    @Test
    void testInitiateClaim_Success() {
        ClaimRequestDTO req = new ClaimRequestDTO();
        req.setPolicyId(5L);
        req.setUserId(2L);

        UserPolicyDTO policy = new UserPolicyDTO();
        policy.setStatus("ACTIVE");
        policy.setUserId(2L);

        UserDTO user = new UserDTO();
        user.setEmail("test@test.com");
        user.setName("Test");

        when(policyClient.getUserPolicyById(5L)).thenReturn(policy);
        when(authClient.getUserById(2L)).thenReturn(user);
        when(claimMapper.mapToEntity(any())).thenReturn(claim);
        when(claimRepository.save(any())).thenReturn(claim);
        when(claimMapper.mapToResponse(any())).thenReturn(new ClaimResponseDTO());
        when(notificationClient.sendEmail(any())).thenReturn(ResponseEntity.ok("Success"));

        assertNotNull(claimService.initiateClaim(req));
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void testInitiateClaim_PolicyNotActive() {
        ClaimRequestDTO req = new ClaimRequestDTO();
        req.setPolicyId(5L);
        UserPolicyDTO policy = new UserPolicyDTO();
        policy.setStatus("PENDING_CANCELLATION");

        when(policyClient.getUserPolicyById(5L)).thenReturn(policy);

        assertThrows(RuntimeException.class, () -> claimService.initiateClaim(req));
    }

    @Test
    void testUpdateClaimStatus_ValidTransition() {
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        
        // No email needed for UNDER_REVIEW based on logic (only APPROVED/REJECTED send email)
        claimService.updateClaimStatus(10L, "UNDER_REVIEW", "testing");

        assertEquals(ClaimStatus.UNDER_REVIEW, claim.getClaimStatus());
    }

    @Test
    void testUpdateClaimStatus_ApprovedSendsEmail() {
        claim.setClaimStatus(ClaimStatus.UNDER_REVIEW);
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        
        UserDTO user = new UserDTO();
        user.setEmail("test@test.com");
        when(authClient.getUserById(2L)).thenReturn(user);
        when(notificationClient.sendEmail(any())).thenReturn(ResponseEntity.ok("OK"));

        claimService.updateClaimStatus(10L, "APPROVED", "Accepted");

        assertEquals(ClaimStatus.APPROVED, claim.getClaimStatus());
        verify(notificationClient, times(1)).sendEmail(any());
    }

    @Test
    void testUpdateClaimStatus_InvalidTransition() {
        // Trying to go directly from SUBMITTED to APPROVED
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        assertThrows(RuntimeException.class, () -> claimService.updateClaimStatus(10L, "APPROVED", "Accepted"));
    }
}
