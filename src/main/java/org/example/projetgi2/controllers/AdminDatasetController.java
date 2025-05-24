package org.example.projetgi2.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.projetgi2.entities.*;
import org.example.projetgi2.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin/datasets")
public class AdminDatasetController {


    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private AnnotateurRepository  annotateurRepository;
    @Autowired
    private AnnotationRepository annotationRepository;


    @GetMapping
    public String listDatasets(Model model) {
        List<Dataset> datasets = datasetRepository.findAll();

        Map<Dataset, Double> avancements = new LinkedHashMap<>();

        for (Dataset dataset : datasets) {
            long TotaleSize = dataset.getTextes().size();
            long annotatedTexts= annotationRepository.countByDatasetId(dataset.getId());
            float avancement = (TotaleSize == 0) ? 0 : ((float) annotatedTexts * 100) / TotaleSize;

            avancements.put(dataset, (double) avancement);
        }
        model.addAttribute("datasets", datasets);
        model.addAttribute("avancements", avancements);

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
    @Transactional
    public String desaffecterAnnotateur(@PathVariable Long datasetId,
                                        @PathVariable Long annotateurId) {

        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));
        Annotateur desaffecte = annotateurRepository.findById(annotateurId)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));

        List<Tache> taches = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotateurId);

        // Récupère les autres annotateurs affectés
        List<Annotateur> autres = dataset.getTaches().stream()
                .map(Tache::getAnnotateur)
                .filter(a -> a != null && !a.equals(desaffecte))
                .distinct()
                .toList();

        for (Tache t : taches) {
            List<CoupleTexte> nonAnnotes = t.getCouples().stream()
                    .filter(ct -> !annotationRepository.existsByAnnotateurAndTexte(desaffecte, ct))
                    .toList();

            if (!autres.isEmpty()) {
                // Distribution circulaire simple
                int i = 0;
                for (CoupleTexte ct : nonAnnotes) {
                    Annotateur cible = autres.get(i % autres.size());

                    // Trouver ou créer la tâche de l'annotateur cible
                    Tache tacheCible = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, cible.getId())
                            .stream().findFirst()
                            .orElseGet(() -> {
                                Tache nouvelle = new Tache();
                                nouvelle.setDataset(dataset);
                                nouvelle.setAnnotateur(cible);
                                nouvelle.setDateLimite(LocalDate.now().plusDays(7));
                                nouvelle.setCouples(new ArrayList<>());
                                return nouvelle;
                            });

                    ct.setTache(tacheCible);
                    tacheCible.getCouples().add(ct);
                    tacheRepository.save(tacheCible);

                    i++;
                }
            }

            // Supprimer le lien avec l'annotateur d'origine
            t.setAnnotateur(null);
            tacheRepository.save(t);
        }

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
                    //bach nskipiw l firt line
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
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
    public String showAffectationForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));

        // Vérifie si le dataset a déjà des tâches affectées
        boolean dejaAffecte = dataset.getTaches().stream()
                .anyMatch(t -> t.getAnnotateur() != null);

        if (dejaAffecte) {
            redirectAttributes.addFlashAttribute("erreur", "❌ Ce dataset est déjà affecté à des annotateurs.");
            return "redirect:/admin/datasets";
        }

        List<Annotateur> allAnnotateurs = annotateurRepository.findByActifTrue();

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

    @GetMapping("/supprimer/{id}")
    public String deleteDataset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Dataset> datasetOpt = datasetRepository.findById(id);
        if (datasetOpt.isPresent()) {
            Dataset dataset = datasetOpt.get();

            // Optional: pre-check if the dataset has tasks with annotations and show warning if needed

            datasetRepository.delete(dataset);
            redirectAttributes.addFlashAttribute("message", "✅ Dataset supprimé avec succès.");
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ Dataset introuvable.");
        }

        return "redirect:/admin/datasets";
    }



}
