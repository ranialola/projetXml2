package fr.univrouen.ProjetXML.controller;

import fr.univrouen.ProjetXML.entities.CV24;
import fr.univrouen.ProjetXML.repository.CV24Repository;
import fr.univrouen.ProjetXML.services.XmlProcessorService;
import fr.univrouen.ProjetXML.services.XmlValidationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import javax.xml.bind.JAXBException;
import java.io.File;

@RestController
@RequestMapping("/api/xml")
public class XmlController {

    private final XmlValidationService xmlValidationService;
    private final XmlProcessorService xmlProcessorService;
    private  final CV24Repository cv24Repository;

    public XmlController(XmlValidationService xmlValidationService, XmlProcessorService xmlProcessorService, CV24Repository cv24Repository) {
        this.xmlValidationService = xmlValidationService;
        this.xmlProcessorService = xmlProcessorService;
        this.cv24Repository = cv24Repository;
    }

/*    @PostMapping("/process")
    public ResponseEntity<?> processXml(@RequestBody String xmlData) {
        if (!xmlValidationService.validateXml(xmlData, "chemin/vers/votre/schema.xsd")) {
            return ResponseEntity.badRequest().body("XML non valide.");
        }

        try {
            Object deserializedObject = xmlProcessorService.deserializeXmlToObj(xmlData, YourJaxbClass.class);
            // Vous pouvez traiter l'objet Java ici, par exemple l'insérer dans une base de données
            return ResponseEntity.ok("XML est valide et a été transformé en objet : " + deserializedObject.toString());
        } catch (JAXBException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la désérialisation : " + e.getMessage());
        }
    }*/

    @PostMapping("/process")
    public ResponseEntity<?> processXml(@RequestBody String xmlData) {
        try {

            File xsdFile = new ClassPathResource("cv24.tp1.xsd").getFile();

            if (!xmlValidationService.validateXml(xmlData, xsdFile.getAbsolutePath())) {
                return ResponseEntity.badRequest().body("XML non valide.");
            }
            return ResponseEntity.ok("XML est valide.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors du chargement du fichier XSD.");
        }
    }
    @PostMapping("/process/add")
    public ResponseEntity<?> processXmlAdd(@RequestBody String xmlData) {
        try {
            // Convertir le XML en entité
            CV24 cv = xmlProcessorService.convertXmlToEntity(xmlData);

            // Vérifier les données
            if (cv.getObjectif() == null || cv.getObjectif().getStatus() == null) {
                throw new RuntimeException("L'objectif ou le statut est nul après la désérialisation.");
            }

            // Sauvegarder l'entité dans la base de données
            cv24Repository.save(cv);

            return ResponseEntity.ok("CV enregistré avec succès !");
        } catch (JAXBException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de la conversion du XML en entité.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de l'enregistrement du CV.");
        }
    }

}




