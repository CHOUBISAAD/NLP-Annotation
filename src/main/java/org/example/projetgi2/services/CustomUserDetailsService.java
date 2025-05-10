package org.example.projetgi2.services;

import org.example.projetgi2.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository,
                                    PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Tentative de connexion avec : " + username);

        return utilisateurRepository.findByLogin(username)
                .map(user -> {
                    System.out.println("✅ Utilisateur trouvé : " + user.getLogin());
                    System.out.println("🧪 Mot de passe DB : " + user.getPassword());
                    System.out.println("🧪 Match admin123 ? " + passwordEncoder.matches("admin123", user.getPassword()));
                    return user;
                })
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));
    }
}
