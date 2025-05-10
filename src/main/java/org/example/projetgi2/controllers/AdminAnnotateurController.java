package org.example.projetgi2.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.projetgi2.entities.*;
import org.example.projetgi2.repositories.AnnotateurRepository;
import org.example.projetgi2.repositories.AnnotationRepository;
import org.example.projetgi2.repositories.RoleRepository;
import org.example.projetgi2.repositories.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/annotateurs")
public class AdminAnnotateurController {
    // gestion CRUD des annotateurs (réservé à l'admin)

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @GetMapping
    public String listAnnotateurs(Model model) {
        model.addAttribute("annotateurs", annotateurRepository.findByActifTrue());
        return "admin/annotateurs/list"; // templates/admin/annotateurs/list.html
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("annotateur", new Annotateur());
        return "admin/annotateurs/form";
    }

    @PostMapping("/save")
    public String saveAnnotateur(@ModelAttribute Annotateur annotateur) {

        System.out.println("Annotateur: " + annotateur);

        Role annotateurRole = roleRepository.findByNomRole("ANNOTATEUR")
                .orElseThrow(() -> new RuntimeException("Role ANNOTATEUR introuvable"));

        annotateur.setRole(annotateurRole);

        // 🔐 encodage du mot de passe
        String encodedPassword = passwordEncoder.encode(annotateur.getPassword());
        annotateur.setPassword(encodedPassword);

        annotateurRepository.save(annotateur);
        return "redirect:/admin/annotateurs";
    }


    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Annotateur annotateur = annotateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé"));
        model.addAttribute("annotateur", annotateur);
        return "admin/annotateurs/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteAnnotateur(@PathVariable Long id) {
        Annotateur annotateur = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));

        annotateur.setActif(false);
        annotateurRepository.save(annotateur);

        return "redirect:/admin/annotateurs";
    }


    @GetMapping("/avancement")
    public String afficherAvancementAnnotateurs(Model model) {
        List<Annotateur> annotateurs = annotateurRepository.findByActifTrue();

        Map<Annotateur, Double> avancements = new LinkedHashMap<>();

        for (Annotateur a : annotateurs) {
            List<Tache> taches = tacheRepository.findByAnnotateur(a);
            long total = taches.stream().mapToLong(t -> t.getCouples().size()).sum();
            long faits = (long) annotationRepository.countByAnnotateur(a);

            double avancement = (total == 0) ? 0 : ((double) faits / total) * 100;
            avancements.put(a, avancement);
        }

        model.addAttribute("avancements", avancements);
        return "admin/annotateurs/avancement";
    }

}