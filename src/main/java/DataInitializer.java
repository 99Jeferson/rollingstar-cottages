

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rollingstar.cottages.model.User;
import com.rollingstar.cottages.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Check if the user registry is empty before seeding
            if (userRepository.count() == 0) {
                System.out.println("--- Seeding User Accounts into PostgreSQL ---");
                
                // Add the 3 Bartenders
                userRepository.save(new User("Brian", "brian2026", "BARTENDER"));
                userRepository.save(new User("James", "james2026", "BARTENDER"));
                userRepository.save(new User("Bosco", "bosco2026", "BARTENDER"));
                
                // Add the Manager
                userRepository.save(new User("Jane", "jane2026", "MANAGER"));
                
                // Add the Boss
                userRepository.save(new User("Olum", "olum2026", "BOSS"));
                
                System.out.println("--- User Seeding Complete! ---");
            }
        };
    }
}