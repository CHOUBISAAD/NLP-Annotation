package org.example.projetgi2.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Le fichier est trop volumineux. Taille maximale autorisée dépassée.");
        return "redirect:/admin/datasets/import";
    }
}

