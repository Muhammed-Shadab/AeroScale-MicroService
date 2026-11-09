package com.Auth.BuyerModule.Repository;


import com.Auth.BuyerModule.Entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, UUID> {
}
