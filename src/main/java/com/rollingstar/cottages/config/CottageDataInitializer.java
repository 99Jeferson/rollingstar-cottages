package com.rollingstar.cottages.config;

import com.rollingstar.cottages.model.Cottage;
import com.rollingstar.cottages.repository.CottageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CottageDataInitializer implements CommandLineRunner {

    private final CottageRepository cottageRepository;

    public CottageDataInitializer(CottageRepository cottageRepository) {
        this.cottageRepository = cottageRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only run if the cottages table is completely fresh
        if (cottageRepository.count() == 0) {
            
            // 1. Generate 8 Standard Rooms (ST-01 to ST-08) @ UGX 150,000
            for (int i = 1; i <= 8; i++) {
                String code = String.format("ST-%02d", i);
                cottageRepository.save(new Cottage(code, "Standard Cottage", 150000, "AVAILABLE", null));
            }

            // 2. Generate 6 Deluxe Rooms (DL-01 to DL-06) @ UGX 250,000
            for (int i = 1; i <= 6; i++) {
                String code = String.format("DL-%02d", i);
                cottageRepository.save(new Cottage(code, "Deluxe Double Room", 250000, "AVAILABLE", null));
            }

            // 3. Generate 4 Suite Rooms (SU-01 to SU-04) @ UGX 400,000
            for (int i = 1; i <= 4; i++) {
                String code = String.format("SU-%02d", i);
                cottageRepository.save(new Cottage(code, "VIP Executive Suite", 400000, "AVAILABLE", null));
            }
            
            System.out.println("✨ Database initialized with 18 default Rolling Star cottages cleanly!");
        }
    }
}