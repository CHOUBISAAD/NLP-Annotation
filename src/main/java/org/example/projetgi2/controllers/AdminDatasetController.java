package org.example.projetgi2.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.projetgi2.entities.*;
import org.example.projetgi2.repositories.AnnotateurRepository;
import org.example.projetgi2.repositories.CoupleTexteRepository;
import org.example.projetgi2.repositories.DatasetRepository;
import org.example.projetgi2.repositories.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/admin/datasets")
public class AdminDatasetController {


    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private AnnotateurRepository  annotateurRepository;

    @GetMapping
    public String listDatasets(Model model) {
        List<Dataset> datasets = datasetRepository.findAll();
        model.addAttribute("datasets", datasets);
        return "admin/datasets/table";
    }

    @GetMapping("/import")
    public String importDataset() {
        return "admin/datasets/import";
    }

    @GetMapping("/{id}")
    public String viewDataset(@PathVariable Long id,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {

        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));

        List<CoupleTexte> allCouples = dataset.getTextes();
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) allCouples.size() / pageSize);

        int start = page * pageSize;
        int end = Math.min(start + pageSize, allCouples.size());
        List<CoupleTexte> couples = allCouples.subList(start, end);

        List<Annotateur> annotateurs = dataset.getTaches().stream()
                .map(Tache::getAnnotateur)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        model.addAttribute("dataset", dataset);
        model.addAttribute("couples", couples);
        model.addAttribute("annotateurs", annotateurs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/datasets/detail";
    }



    @PostMapping("/{datasetId}/desaffecter/{annotateurId}")
    public String desaffecterAnnotateur(@PathVariable Long datasetId,
                                        @PathVariable Long annotateurId) {
        List<Tache> taches = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotateurId);

        for (Tache t : taches) {
            t.setAnnotateur(null); // On retire le lien, mais on ne supprime pas la tâche ni les annotations
        }

        tacheRepository.saveAll(taches);

        return "redirect:/admin/datasets/" + datasetId;
    }


    @PostMapping("/import")
    public String importDataset(@RequestParam("file") MultipartFile file,
                                @RequestParam String nomDataset,
                                @RequestParam(required = false) String description,
                                @RequestParam("classes") String classesStr ) throws IOException {
        if (file.isEmpty()) return "redirect:/admin/datasets/import?error";

        Dataset dataset = new Dataset();
        dataset.setNomDataset(nomDataset);
        dataset.setDescription(description);

        List<CoupleTexte> textes = new ArrayList<>();

        String filename = file.getOriginalFilename();

        if (filename.endsWith(".csv")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // skip header
                    }
                    String[] columns = line.split(",");
                    if (columns.length >= 2) {
                        CoupleTexte ct = new CoupleTexte();
                        ct.setTexte1(columns[0].trim());
                        ct.setTexte2(columns[1].trim());
                        ct.setDataset(dataset);
                        textes.add(ct);
                    }
                }

            }
        } else if (filename.endsWith(".json")) {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> data = mapper.readValue(file.getInputStream(), List.class);
            for (Map<String, String> entry : data) {
                CoupleTexte ct = new CoupleTexte();
                ct.setTexte1(entry.get("texte1"));
                ct.setTexte2(entry.get("texte2"));
                ct.setDataset(dataset);
                textes.add(ct);
            }
        }

        List<ClassePossible> classePossibles = new ArrayList<>();
        String[] classes = classesStr.split(";");
        for (String classe : classes) {
            ClassePossible cp = new ClassePossible();
            cp.setNomClasse(classe.trim());
            cp.setDataset(dataset); // lien inverse
            classePossibles.add(cp);
        }
        dataset.setClasses(classePossibles);
        dataset.setTextes(textes);
        datasetRepository.save(dataset); // grâce au cascade, les textes seront enregistrés

        return "redirect:/admin/datasets/import?success";
    }

    //affectation des annotateurs
    @GetMapping("/{id}/affecter")
    public String showAffectationForm(@PathVariable Long id, Model model) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));
        List<Annotateur> allAnnotateurs = annotateurRepository.findAll();

        model.addAttribute("dataset", dataset);
        model.addAttribute("annotateurs", allAnnotateurs);
        return "admin/datasets/affecter";
    }

    @PostMapping("/{id}/affecter")
    @Transactional
    public String affecterAnnotateurs(@PathVariable Long id,
                                      @RequestParam("annotateurIds") List<Long> annotateurIds) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));
        List<CoupleTexte> textes = dataset.getTextes();
        List<Annotateur> annotateurs = annotateurRepository.findAllById(annotateurIds);

        int totalTextes = textes.size();
        int textesParAnnotateur = totalTextes / annotateurs.size();
        int reste = totalTextes % annotateurs.size();
        int index = 0;

        for (Annotateur annotateur : annotateurs) {
            int count = textesParAnnotateur + (reste-- > 0 ? 1 : 0);
            List<CoupleTexte> sousListe = textes.subList(index, index + count);

            Tache tache = new Tache();
            tache.setAnnotateur(annotateur);
            tache.setDataset(dataset);
            tache.setDateLimite(LocalDate.now().plusDays(7));
            tache.setCouples(new ArrayList<>(sousListe));

            for (CoupleTexte ct : sousListe) ct.setTache(tache);

            tacheRepository.save(tache);
            index += count;
        }

        return "redirect:/admin/datasets/" + id;
    }



}
