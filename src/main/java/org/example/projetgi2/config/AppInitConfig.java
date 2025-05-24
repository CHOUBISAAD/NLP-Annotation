package org.example.projetgi2.config;

import org.example.projetgi2.entities.Administrateur;
import org.example.projetgi2.entities.Role;
import org.example.projetgi2.repositories.RoleRepository;
import org.example.projetgi2.repositories.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppInitConfig {

    @Bean
    public CommandLineRunner initAdmin(UtilisateurRepository utilisateurRepository,
                                       RoleRepository roleRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            // Create "ADMIN" role if it doesn't exist
            Role adminRole = roleRepository.findByNomRole("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

            // Check if admin already exists
            if (utilisateurRepository.findByLogin("admin").isEmpty()) {
                Administrateur admin = new Administrateur();
                admin.setNom("Admin");
                admin.setPrenom("Principal");
                admin.setLogin("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(adminRole);
                System.out.println("Mot de passe encodé : " + admin.getPassword());

                utilisateurRepository.save(admin);
                System.out.println("✅ Admin user created: login=admin, password=admin123");
            }
        };
    }
}

