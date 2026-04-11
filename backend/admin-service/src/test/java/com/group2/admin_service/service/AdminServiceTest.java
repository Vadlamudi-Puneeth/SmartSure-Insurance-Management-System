package com.group2.admin_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.group2.admin_service.dto.*;
import com.group2.admin_service.feign.*;
import com.group2.admin_service.service.impl.AdminServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock private AuthFeignClient authClient;
    @Mock private ClaimsFeignClient claimClient;
    @Mock private PolicyFeignClient policyClient;
    @Mock private NotificationFeignClient notificationClient;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void testClaimOps() {
        when(claimClient.getAllClaims()).thenReturn(List.of(new ClaimDTO()));
        assertNotNull(adminService.getAllClaims());
        
        when(claimClient.getClaimStatus(1L)).thenReturn(new ClaimDTO());
        assertNotNull(adminService.getClaimStatus(1L));
        
        when(claimClient.getClaimsByUserId(1L)).thenReturn(List.of(new ClaimDTO()));
        assertNotNull(adminService.getClaimsByUserId(1L));
        
        when(claimClient.downloadDocument(1L)).thenReturn(ResponseEntity.ok(new byte[0]));
        assertNotNull(adminService.downloadClaimDocument(1L));
    }

    @Test
    void testReviewClaim() {
        ReviewRequest req = new ReviewRequest(); req.setStatus("APPROVED"); req.setRemark("ok");
        ClaimDTO c = new ClaimDTO(); c.setUserId(1L);
        UserDTO u = new UserDTO(); u.setEmail("t@t.com");
        
        when(claimClient.getClaimById(1L)).thenReturn(c);
        when(authClient.getUserById(1L)).thenReturn(u);
        
        adminService.reviewClaim(1L, req);
        verify(notificationClient).sendEmail(any());
        
        // Notify fail
        doThrow(new RuntimeException()).when(notificationClient).sendEmail(any());
        adminService.reviewClaim(1L, req);
    }

    @Test
    void testReviewClaimSkipsNotificationWhenClaimOrUserIncomplete() {
        ReviewRequest req = new ReviewRequest();
        req.setStatus("REJECTED");
        req.setRemark("no");

        when(claimClient.getClaimById(10L)).thenReturn(null);
        adminService.reviewClaim(10L, req);
        verify(notificationClient, never()).sendEmail(any());

        ClaimDTO noUser = new ClaimDTO();
        noUser.setUserId(null);
        when(claimClient.getClaimById(11L)).thenReturn(noUser);
        adminService.reviewClaim(11L, req);
        verify(notificationClient, never()).sendEmail(any());

        ClaimDTO withUser = new ClaimDTO();
        withUser.setUserId(99L);
        when(claimClient.getClaimById(12L)).thenReturn(withUser);
        when(authClient.getUserById(99L)).thenReturn(null);
        adminService.reviewClaim(12L, req);
        verify(notificationClient, never()).sendEmail(any());

        UserDTO noEmail = new UserDTO();
        noEmail.setEmail(null);
        when(claimClient.getClaimById(13L)).thenReturn(withUser);
        when(authClient.getUserById(99L)).thenReturn(noEmail);
        adminService.reviewClaim(13L, req);
        verify(notificationClient, never()).sendEmail(any());
    }

    @Test
    void testReviewClaimInnerTryCatchesWhenFeignFails() {
        ReviewRequest req = new ReviewRequest();
        req.setStatus("APPROVED");
        req.setRemark("x");
        when(claimClient.getClaimById(20L)).thenThrow(new RuntimeException("down"));
        adminService.reviewClaim(20L, req);
        verify(notificationClient, never()).sendEmail(any());
    }

    @Test
    void testPolicyOps() {
        PolicyRequestDTO req = new PolicyRequestDTO();
        when(policyClient.createPolicy(any())).thenReturn(new PolicyDTO());
        assertNotNull(adminService.createPolicy(req));
        
        when(policyClient.updatePolicy(anyLong(), any())).thenReturn(new PolicyDTO());
        assertNotNull(adminService.updatePolicy(1L, req));
        
        adminService.deletePolicy(1L);
        verify(policyClient).deletePolicy(1L);
    }

    @Test
    void testUserAndReports() {
        when(authClient.getAllUsers()).thenReturn(List.of(new UserDTO()));
        assertNotNull(adminService.getAllUsers());
        assertNotNull(adminService.getFilteredUsers(0, 5, "", null, null));
        
        when(claimClient.getAllClaims()).thenReturn(List.of(new ClaimDTO()));
        ReportResponse r = adminService.getReports();
        assertEquals(1, r.getTotalClaims());
        
        // Fail path (doThrow/doReturn avoid invoking a stub that still throws)
        doThrow(new RuntimeException()).when(authClient).getAllUsers();
        adminService.getReports();

        doReturn(List.of()).when(authClient).getAllUsers();
        when(claimClient.getAllClaims()).thenReturn(null);
        ReportResponse noClaims = adminService.getReports();
        assertEquals(0, noClaims.getTotalClaims());
    }
}
