package org.example.projetgi2.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.projetgi2.entities.*;
import org.example.projetgi2.repositories.*;
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

    @Autowired
    private DatasetRepository datasetRepository;

    @GetMapping
    public String listAnnotateurs(Model model) {
        model.addAttribute("annotateurs", annotateurRepository.findAll());
        return "admin/annotateurs/list";
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

    //suppression logique
    @GetMapping("/delete/{id}")
    public String deleteAnnotateur(@PathVariable Long id) {
        Annotateur annotateur = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));

        annotateur.setActif(false);
        annotateurRepository.save(annotateur);

        return "redirect:/admin/annotateurs";
    }

    //suppression definitive
    @GetMapping("/delete-definitely/{id}")
    public String deleteAnnotateurDefinitely(@PathVariable Long id) {
        Annotateur annotateur =  annotateurRepository.findById(id).orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
        annotateurRepository.delete(annotateur);
        return "redirect:/admin/annotateurs";
    }

    @GetMapping("/activate-annotateur/{id}")
    public String activerAnnotateur(@PathVariable Long id) {
        Annotateur annotateur =  annotateurRepository.findById(id).orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
        annotateur.setActif(true);
        annotateurRepository.save(annotateur);
        return "redirect:/admin/annotateurs";
    }



    @GetMapping("/avancement")
    public String afficherSelectionDataset(Model model) {
        List<Dataset> datasets = datasetRepository.findAll();
        model.addAttribute("datasets", datasets);
        return "admin/annotateurs/select_dataset_avancement";
    }
    @PostMapping("/avancement")
    public String afficherAvancementPourDataset(@RequestParam Long datasetId, Model model) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));

        List<Annotateur> annotateurs = annotateurRepository.findByActifTrue();

        Map<Annotateur, Double> avancements = new LinkedHashMap<>();

        for (Annotateur a : annotateurs) {
            List<Tache> taches = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, a.getId());

            long total = taches.stream().mapToLong(t -> t.getCouples().size()).sum();
            long faits = taches.stream()
                    .flatMap(t -> t.getCouples().stream())
                    .mapToLong(c -> annotationRepository.countByAnnotateurAndTexte(a, c))
                    .sum();

            double avancement = (total == 0) ? 0 : ((double) faits / total) * 100;
            avancements.put(a, avancement);
        }

        model.addAttribute("dataset", dataset);
        model.addAttribute("avancements", avancements);
        return "admin/annotateurs/avancement";
    }


}