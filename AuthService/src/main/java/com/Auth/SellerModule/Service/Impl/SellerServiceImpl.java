package com.Auth.SellerModule.Service.Impl;


import com.Auth.BuyerModule.DTO.Request.ProductDetails;
import com.Auth.Client.OrderClient;
import com.Auth.Client.ProductClient;
import com.Auth.Exceptions.BuyerException.RequiredThingsNotFoundException;
import com.Auth.SellerModule.DTO.Request.UpdateProfileRequest;
import com.Auth.SellerModule.DTO.Request.UpdateSellerMetricsRequest;
import com.Auth.SellerModule.DTO.Response.*;
import com.Auth.SellerModule.Entity.Seller;
import com.Auth.SellerModule.Repository.SellerRepository;
import com.Auth.SellerModule.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final OrderClient orderClient;
    private final ProductClient productClient;

    @Override
    @Transactional
    public void updateProfile(UpdateProfileRequest updateProfileRequest, UUID id) {
        Seller seller = sellerRepository.findById(id).get();

        seller.setDateOfBirth(updateProfileRequest.getDOB());
        seller.setBussinessName(updateProfileRequest.getBussinessName());

        if(updateProfileRequest.getBussinessAddress() != null) seller.setBussinessAddress(updateProfileRequest.getBussinessAddress());
        if(updateProfileRequest.getWareHousePincode() != null) seller.setWareHousePinCode(updateProfileRequest.getWareHousePincode());

        sellerRepository.save(seller);
    }

    @Override
    public ProfileResponse getProfile(UUID id, String email) {
        Seller seller = findSellerOrThrow(id);

        return ProfileResponse.builder()
                .id(id.toString())
                .email(email)
                .bussinessName(seller.getBussinessName())
                .bussinessAddress(seller.getBussinessAddress())
                .dob(seller.getDateOfBirth())
                .verificationStatus(seller.getSellerVerificationStatus().toString())
                .rating(seller.getRating())
                .totalRevenue(seller.getTotalRevenew())
                .maxConcurrentFlashSaleSlots(seller.getMaxConcurrentFlashSaleSlots())
                .createdAt(seller.getCreatedAt())
                .totalOrdersFullFilled(seller.getTotalOrdersFullFilled())
                .build();
    }

    public Seller findSellerOrThrow(UUID id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new RequiredThingsNotFoundException("User Not Found"));
    }

    @Override
    @Cacheable(value = "Seller:Analytics", key = "#id + '_' + #days" )
    public AnalyticsResponse getDataForAnalytics(UUID id, int days) {

        Instant end = Instant.now();
        Instant start = end.minus(days, ChronoUnit.DAYS);

        List<OrderItemResponse> orderItemList = orderClient.getOrderItemsBetweenThisDuration(id, start, end);
        System.out.println("fine");
        List<OrderItemResponse> prevOrderItems = orderClient.getOrderItemsBetweenThisDuration(id, start.minus(days+1, ChronoUnit.DAYS), start.minus(1, ChronoUnit.DAYS));

        durationSnapShot snapShot1 = statusOfDuration(orderItemList);
        durationSnapShot snapshot2 = statusOfDuration(prevOrderItems);

        AnalyticsSummary summary = AnalyticsSummary.builder()
                .revenue(snapShot1.totalRevenue)
                .orders(snapShot1.totalOrders())
                .previousOrders(snapshot2.totalOrders)
                .previousUnitsSold(snapshot2.unitsSold())
                .previousRevenue(snapshot2.totalRevenue)
                .unitsSold(snapShot1.unitsSold)
                .build();

        BigDecimal[] revenuePerDay = new BigDecimal[days+1];
        int[] ordersPerDay = new int[days+1];
        int[] unitsSoldPerDay = new int[days+1];

        StatusBreakdown confirmedStatus = new StatusBreakdown("CONFIRMED", 0);
        StatusBreakdown shippedStatus = new StatusBreakdown("SHIPPED", 0);
        StatusBreakdown deliveredStatus = new StatusBreakdown("DELIVERED", 0);
        StatusBreakdown cancelledStatus = new StatusBreakdown("CANCELLED", 0);

        LocalDate startDate = start.atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        BigDecimal revenueByFlashSale = BigDecimal.ZERO, revenueByNormal = BigDecimal.ZERO;
        for(OrderItemResponse oi: orderItemList) {

            if(oi.getStatus().compareTo(OrderItemResponse.OrderItemStatus.CONFIRMED) == 0)
                confirmedStatus.setCount(confirmedStatus.getCount() + 1);
            else if(oi.getStatus().compareTo(OrderItemResponse.OrderItemStatus.CANCELLED) == 0){
                cancelledStatus.setCount(cancelledStatus.getCount() + 1);
                continue;
            }
            if(oi.getStatus().compareTo(OrderItemResponse.OrderItemStatus.DELIVERED) == 0)
                deliveredStatus.setCount(deliveredStatus.getCount() + 1);
            if(oi.getStatus().compareTo(OrderItemResponse.OrderItemStatus.SHIPPED) == 0)
                shippedStatus.setCount(shippedStatus.getCount() + 1);


            LocalDate dateOfOrder = oi.getCreatedAt().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
            int diff = (int)ChronoUnit.DAYS.between(startDate, dateOfOrder);

            if(revenuePerDay[diff] == null) revenuePerDay[diff] = BigDecimal.ZERO;
            revenuePerDay[diff] = revenuePerDay[diff].add(oi.getSubTotal());
            ordersPerDay[diff]++;
            unitsSoldPerDay[diff] += oi.getQuantity();

            if(oi.getIsFlashSaleProduct()) revenueByFlashSale = revenueByFlashSale.add(oi.getSubTotal());
            else revenueByNormal = revenueByNormal.add(oi.getSubTotal());

        }

        List<DailyAnalytics> dailyAnalyticsList = new ArrayList<>();
        List<StatusBreakdown> statusBreakdowns = List.of(confirmedStatus, shippedStatus, cancelledStatus, deliveredStatus);

        for(int i=0;i<=days;i++) {
            dailyAnalyticsList.add(DailyAnalytics.builder()
                    .date(startDate)
                    .revenue(revenuePerDay[i])
                    .unitsSold(unitsSoldPerDay[i])
                    .orders(ordersPerDay[i])
                    .build());

            startDate = startDate.plusDays(1);
        }



        AnalyticsResponse response = AnalyticsResponse.builder()
                .rangeDays(days)
                .from(start.atZone(ZoneId.of("Asia/Kolkata")).toLocalDate())
                .to(end.atZone(ZoneId.of("Asia/Kolkata")).toLocalDate())
                .summary(summary)
                .daily(dailyAnalyticsList)
                .statusBreakdown(statusBreakdowns)
                .topProducts(getTopProductsForAnalytics(orderItemList))
                .revenueBySource(new RevenueBySource(revenueByFlashSale, revenueByNormal))
                .build();

        return response;
    }

    @Override
    @Transactional
    public void batchUpdateRevenueAndFulfilled(List<UpdateSellerMetricsRequest> updates) {
        if (updates == null || updates.isEmpty()) return;

        // 1. Saare seller IDs
        List<UUID> sellerIds = updates.stream()
                .map(UpdateSellerMetricsRequest::getSellerId)
                .toList();

        // 2. Single DB Query me saare sellers fetch
        List<Seller> sellers = sellerRepository.findAllById(sellerIds);
        Map<UUID, Seller> sellerMap = sellers.stream()
                .collect(Collectors.toMap(Seller::getId, Function.identity()));

        // 3. In-memory update
        for (UpdateSellerMetricsRequest update : updates) {
            Seller seller = sellerMap.get(update.getSellerId());
            if (seller != null) {
                if (update.getRevenue() != null) {
                    seller.setTotalRevenew(seller.getTotalRevenew().add(update.getRevenue()));
                }
                if (update.getFulfilledCount() != null) {
                    seller.setTotalOrdersFullFilled(seller.getTotalOrdersFullFilled() + update.getFulfilledCount());
                }
            }
        }

        sellerRepository.saveAll(sellers);
    }

    public List<TopProductsAnalytics> getTopProductsForAnalytics(List<OrderItemResponse> orderItemList) {
        Map<String, BigDecimal> revenue = new HashMap<>();
        Map<String, Long> units = new HashMap<>();

        for(OrderItemResponse oi: orderItemList) {
            String id = oi.getProductId().toString();
            if(oi.getStatus().toString().equals("PENDING") || oi.getStatus().toString().equals("CANCELLED")) continue;
            revenue.put(id, revenue.getOrDefault(id, BigDecimal.ZERO)
                    .add(oi.getSubTotal()));
            units.put(id, units.getOrDefault(id, 0L)*1L +
                    oi.getQuantity());
        }

        PriorityQueue<String> q = new PriorityQueue<>((a,b) -> {
            return revenue.get(a).compareTo(revenue.get(b));
        });

        for(String id: revenue.keySet()) {
            q.add(id);
            while(q.size() > 5) q.poll();
        }

        List<TopProductsAnalytics> res = new ArrayList<>();
        while(!q.isEmpty()) {
            String id = q.poll();
            ProductDetails product = productClient.getProductDetails(UUID.fromString(id));

            res.add(TopProductsAnalytics.builder()
                            .productId(id)
                            .imageUrl(product.getImageUrl())
                            .name(product.getName())
                            .unitsSold(units.get(id))
                            .revenue(revenue.get(id))
                            .build());

        }
        Collections.reverse(res);
        return res;
    }

    public record durationSnapShot(
        BigDecimal totalRevenue, long unitsSold, int totalOrders
    ) {}

    public durationSnapShot statusOfDuration(List<OrderItemResponse> orderItemList) {
        BigDecimal totalRevenew = BigDecimal.ZERO;
        long unitsSold = 0;
        int totalOrders = 0;
        for(OrderItemResponse orderItem: orderItemList) {
            if(orderItem.getStatus().toString().equals("CANCELLED") || orderItem.getStatus().toString().equals("PENDING")) continue;

            totalRevenew = totalRevenew.add(orderItem.getSubTotal());
            unitsSold += orderItem.getQuantity();
            totalOrders++;
        }

        return new durationSnapShot(totalRevenew, unitsSold, totalOrders);
    }
}
