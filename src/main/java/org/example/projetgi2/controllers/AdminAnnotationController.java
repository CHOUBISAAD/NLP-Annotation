package org.example.projetgi2.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.example.projetgi2.entities.Annotation;
import org.example.projetgi2.entities.CoupleTexte;
import org.example.projetgi2.repositories.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminAnnotationController {
    @Autowired
    private AnnotationRepository annotationRepository;

    @GetMapping("/annotations/export")
    public void exporterAnnotations(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=annotations.csv");

        List<Annotation> annotations = annotationRepository.findAll();
        try (PrintWriter writer = response.getWriter()) {
            writer.println("text1,text2,annotation");
            for (Annotation ann : annotations) {
                CoupleTexte texte = ann.getTexte();
                writer.printf("\"%s\",\"%s\",\"%s\"\n",
                        texte.getTexte1().replace("\"", "\"\""),
                        texte.getTexte2().replace("\"", "\"\""),
                        ann.getChoixChoisi().replace("\"", "\"\""));
            }
        }
    }
}
