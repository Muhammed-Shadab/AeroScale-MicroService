package com.Auth.SellerModule.Controller;

import com.Auth.SellerModule.DTO.Request.UpdateProfileRequest;
import com.Auth.SellerModule.DTO.Request.UpdateSellerMetricsRequest;
import com.Auth.SellerModule.DTO.Response.AnalyticsResponse;
import com.Auth.SellerModule.DTO.Response.ProfileResponse;
import com.Auth.SellerModule.Service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest updateProfileRequest,
                                           @AuthenticationPrincipal(expression = "id") UUID id) {
        sellerService.updateProfile(updateProfileRequest, id);
        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal(expression = "id") UUID id ,
                                                      @AuthenticationPrincipal(expression= "email") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerService.getProfile(id, email));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getDataForAnalytics(@AuthenticationPrincipal(expression = "id") UUID id
                                    , @RequestParam int days) {
        return ResponseEntity.ok(sellerService.getDataForAnalytics(id, days));
    }

    @PutMapping("/internal/batch-update-metrics")
    public ResponseEntity<Void> batchUpdateSellerMetrics(@RequestBody List<UpdateSellerMetricsRequest> updates) {
        sellerService.batchUpdateRevenueAndFulfilled(updates);
        return ResponseEntity.ok().build();
    }

}
