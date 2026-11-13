package com.Auth.SellerModule.Service;


import com.Auth.SellerModule.DTO.Request.UpdateProfileRequest;
import com.Auth.SellerModule.DTO.Request.UpdateSellerMetricsRequest;
import com.Auth.SellerModule.DTO.Response.AnalyticsResponse;
import com.Auth.SellerModule.DTO.Response.ProfileResponse;
import com.Auth.SellerModule.Entity.Seller;

import java.util.List;
import java.util.UUID;

public interface SellerService {
    void updateProfile(UpdateProfileRequest updateProfileRequest, UUID id);

    ProfileResponse getProfile(UUID id, String email);

    Seller findSellerOrThrow(UUID id);

    AnalyticsResponse getDataForAnalytics(UUID id, int days);

    void batchUpdateRevenueAndFulfilled(List<UpdateSellerMetricsRequest> updates);
}
