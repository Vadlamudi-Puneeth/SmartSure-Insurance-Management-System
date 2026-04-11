package com.group2.admin_service.service.impl;

import com.group2.admin_service.dto.*;
import com.group2.admin_service.feign.*;
import com.group2.admin_service.service.IAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdminServiceImpl implements IAdminService {

    private final AuthFeignClient authClient;
    private final ClaimsFeignClient claimClient;
    private final PolicyFeignClient policyClient;
    private final NotificationFeignClient notificationClient;
    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    public AdminServiceImpl(AuthFeignClient authClient, ClaimsFeignClient claimClient, PolicyFeignClient policyClient, NotificationFeignClient notificationClient) {
        this.authClient = authClient;
        this.claimClient = claimClient;
        this.policyClient = policyClient;
        this.notificationClient = notificationClient;
    }

    @Override public List<UserDTO> getAllUsers() { return authClient.getAllUsers(); }

    @Override
    public List<ClaimDTO> getAllClaims() { return claimClient.getAllClaims(); }

    @Override
    public void reviewClaim(Long id, ReviewRequest req) {
        claimClient.updateClaimStatus(id, new ClaimStatusUpdateDTO(req.getStatus(), req.getRemark()));
        try {
            ClaimDTO c = claimClient.getClaimById(id);
            if (c != null && c.getUserId() != null) {
                UserDTO u = authClient.getUserById(c.getUserId());
                if (u != null && u.getEmail() != null) {
                    notificationClient.sendEmail(new EmailRequest(u.getEmail(), "Claim Reviewed", "Status: " + req.getStatus()));
                }
            }
        } catch (Exception e) { log.error("Notify fail: {}", e.getMessage()); }
    }

    @Override public ClaimDTO getClaimStatus(Long id) { return claimClient.getClaimStatus(id); }
    @Override public List<ClaimDTO> getClaimsByUserId(Long id) { return claimClient.getClaimsByUserId(id); }
    @Override public ResponseEntity<byte[]> downloadClaimDocument(Long id) { return claimClient.downloadDocument(id); }

    @Override public PolicyDTO createPolicy(PolicyRequestDTO dto) { return policyClient.createPolicy(dto); }
    @Override public PolicyDTO updatePolicy(Long id, PolicyRequestDTO dto) { return policyClient.updatePolicy(id, dto); }
    @Override public void deletePolicy(Long id) { policyClient.deletePolicy(id); }

    @Override
    public java.util.Map<String, Object> getFilteredUsers(int p, int s, String q, String ps, String cs) {
        Map<String, Object> res = new HashMap<>();
        res.put("users", authClient.getAllUsers());
        return res;
    }

    @Override
    public ReportResponse getReports() {
        ReportResponse r = new ReportResponse();
        try {
            List<UserDTO> users = authClient.getAllUsers();
            List<ClaimDTO> claims = claimClient.getAllClaims();
            if (claims != null) r.setTotalClaims(claims.size());
        } catch (Exception e) { log.error("Report fail: {}", e.getMessage()); }
        return r;
    }
}