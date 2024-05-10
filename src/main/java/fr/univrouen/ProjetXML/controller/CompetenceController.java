package fr.univrouen.ProjetXML.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.univrouen.ProjetXML.entities.Competence;
import fr.univrouen.ProjetXML.services.XmlProcessorService;

@RestController
@RequestMapping("/competences")
public class CompetenceController {

    private final  XmlProcessorService competenceService;

    @Autowired
    public CompetenceController(XmlProcessorService competenceService) {
        this.competenceService = competenceService;
    }

  
}
