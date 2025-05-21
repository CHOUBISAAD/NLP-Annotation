package org.example.projetgi2.controllers;

import org.example.projetgi2.entities.Annotateur;
import org.example.projetgi2.entities.Dataset;
import org.example.projetgi2.entities.Tache;
import org.example.projetgi2.repositories.AnnotateurRepository;
import org.example.projetgi2.repositories.AnnotationRepository;
import org.example.projetgi2.repositories.DatasetRepository;
import org.example.projetgi2.repositories.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    @GetMapping("/home")
    public String adminHome(@RequestParam(name = "datasetId", required = false) Long datasetId, Model model) {
        List<Annotateur> annotateurs = annotateurRepository.findByActifTrue();
        List<Dataset> datasets = datasetRepository.findAll();

        Dataset selected = (datasetId != null)
                ? datasetRepository.findById(datasetId).orElse(null)
                : datasets.isEmpty() ? null : datasets.get(0);

        List<String> noms = new ArrayList<>();
        List<Double> pourcentages = new ArrayList<>();

        if (selected != null) {
            for (Annotateur a : annotateurs) {
                List<Tache> taches = tacheRepository.findByDatasetIdAndAnnotateurId(selected.getId(), a.getId());
                long total = taches.stream().mapToLong(t -> t.getCouples().size()).sum();
                long faits = annotationRepository.countByAnnotateurAndTachesIn(a, taches);
                double avancement = (total == 0) ? 0 : (faits * 100.0) / total;
                noms.add(a.getNom() + " " + a.getPrenom());
                pourcentages.add(avancement);
            }
        }

        model.addAttribute("annotateurs", annotateurs);
        model.addAttribute("annotateurCount", annotateurs.size());
        model.addAttribute("datasetCount", datasets.size());
        model.addAttribute("datasets", datasets);
        model.addAttribute("selectedDatasetId", selected != null ? selected.getId() : null);
        model.addAttribute("datasetLabel", selected != null ? selected.getNomDataset() : "Aucun");
        model.addAttribute("chartLabels", noms);
        model.addAttribute("chartData", pourcentages);

        return "admin/home";
    }

}

