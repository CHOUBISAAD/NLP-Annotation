package org.example.projetgi2.controllers;

import org.example.projetgi2.entities.Annotateur;
import org.example.projetgi2.entities.Annotation;
import org.example.projetgi2.entities.CoupleTexte;
import org.example.projetgi2.entities.Tache;
import org.example.projetgi2.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/annotateur")
public class AnnotateurController {
    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private CoupleTexteRepository coupleTexteRepository;
    @Autowired
    private AnnotationRepository annotationRepository;


    @GetMapping("/home")
    public String annotateurHome(Model model, @AuthenticationPrincipal Annotateur annotateur) {
        List<Tache> taches = tacheRepository.findByAnnotateur(annotateur);

        List<Map<String, Object>> charts = new ArrayList<>();

        for (Tache t : taches) {
            int total = t.getCouples().size();
            int faits = (int) annotationRepository.countByAnnotateurAndTache(annotateur, t);
            int pourcentage = total > 0 ? (int) Math.round((faits * 100.0) / total) : 0;

            Map<String, Object> chart = new HashMap<>();
            chart.put("label", t.getDataset().getNomDataset());
            chart.put("pourcentage", pourcentage);
            chart.put("tacheId", t.getId());
            charts.add(chart);
        }

        model.addAttribute("charts", charts);
        return "annotateur/home";
    }





    @GetMapping("/mes-taches")
    public String mesTaches(Model model, @AuthenticationPrincipal Annotateur annotateur) {
        List<Tache> taches = tacheRepository.findByAnnotateur(annotateur);

        // Initialiser les totaux
        Map<Long, Integer> avancements = new HashMap<>();
        Map<Long, Integer> faitsParTache = new HashMap<>();

        // Requête optimisée
        List<Object[]> counts = annotationRepository.countAnnotationsByTache(annotateur, taches);
        for (Object[] row : counts) {
            Long tacheId = (Long) row[0];
            Long nbFaits = (Long) row[1];
            faitsParTache.put(tacheId, nbFaits.intValue());
        }

        // Calculer les % à partir de faits / total
        for (Tache t : taches) {
            int total = t.getCouples().size();
            int faits = faitsParTache.getOrDefault(t.getId(), 0);
            int pourcentage = total > 0 ? (int) Math.round(faits * 100.0 / total) : 0;
            avancements.put(t.getId(), pourcentage);
        }

        model.addAttribute("taches", taches);
        model.addAttribute("avancements", avancements);
        model.addAttribute("faitsParTache", faitsParTache);
        return "annotateur/taches";
    }



    @GetMapping("/annoter/{tacheId}")
    public String travailler(@PathVariable Long tacheId,
                             @RequestParam(defaultValue = "0") int index,
                             @RequestParam(defaultValue = "non-annotes") String filtre,
                             @AuthenticationPrincipal Annotateur annotateur,
                             Model model) {

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));

        List<CoupleTexte> tous = tache.getCouples();

        // Filtrage
        List<CoupleTexte> couples = "tous".equals(filtre) ? tous :
                tous.stream()
                        .filter(ct -> !annotationRepository.existsByAnnotateurAndTexte(annotateur, ct))
                        .toList();

        if (couples.isEmpty()) {
            model.addAttribute("message", "✅ Tous les couples ont été annotés !");
            return "annotateur/travailler";
        }

        if (index < 0 || index >= couples.size()) index = 0;
        CoupleTexte courant = couples.get(index);

        Annotation annotationExistante = annotationRepository
                .findByAnnotateurAndTexte(annotateur, courant)
                .orElse(null);

        model.addAttribute("filtre", filtre);
        model.addAttribute("annotationExistante", annotationExistante);
        model.addAttribute("tache", tache);
        model.addAttribute("dataset", tache.getDataset());
        model.addAttribute("couple", courant);
        model.addAttribute("index", index);
        model.addAttribute("total", couples.size());
        model.addAttribute("classes", tache.getDataset().getClasses());

        return "annotateur/travailler";
    }



    @PostMapping("/annoter/{tacheId}")
    public String soumettreAnnotation(@PathVariable Long tacheId,
                                      @RequestParam Long coupleId,
                                      @RequestParam(required = false) String choix,
                                      @RequestParam int index,
                                      @RequestParam String action,
                                      @RequestParam(defaultValue = "non-annotes") String filtre,
                                      Principal principal) {

        Annotateur annotateur = (Annotateur) utilisateurRepository.findByLogin(principal.getName()).orElseThrow();
        CoupleTexte couple = coupleTexteRepository.findById(coupleId).orElseThrow();

        if ("valider".equals(action) && choix != null && !choix.isBlank()) {
            Annotation annotation = annotationRepository.findByAnnotateurAndTexte(annotateur, couple)
                    .orElse(null);

            if (annotation == null) {
                annotation = new Annotation();
                annotation.setAnnotateur(annotateur);
                annotation.setTexte(couple);
            }
            annotation.setChoixChoisi(choix); // mise à jour ou création
            annotationRepository.save(annotation);
        }

        int newIndex = index;
        if ("valider".equals(action)) newIndex++; // auto-avance
        if ("suivant".equals(action)) newIndex++;
        else if ("precedent".equals(action)) newIndex--;

        return "redirect:/annotateur/annoter/" + tacheId + "?index=" + newIndex + "&filtre=" + filtre;
    }




}
