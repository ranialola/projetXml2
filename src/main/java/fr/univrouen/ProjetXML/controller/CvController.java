package fr.univrouen.ProjetXML.controller;

import fr.univrouen.ProjetXML.entities.CV24;
import fr.univrouen.ProjetXML.entities.Certif;
import fr.univrouen.ProjetXML.entities.Competence;
import fr.univrouen.ProjetXML.entities.Diplome;
import fr.univrouen.ProjetXML.repository.CV24Repository;
import fr.univrouen.ProjetXML.repository.CompetenceRepository;
import fr.univrouen.ProjetXML.services.XmlProcessorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
@AllArgsConstructor
//@RestController
public class CvController {
    @Autowired
    private CV24Repository cv24Repository;
    @Autowired
    private CompetenceRepository competenceRepository;

    @Autowired
    private XmlProcessorService xmlProcessorService;

    @GetMapping("/cvs")
    public String getAllCv24(Model model) {

        List<CV24> CV24 = cv24Repository.findAll();
        model.addAttribute("CV24", CV24);

        return "cvs";

    }

    /* @GetMapping(value = "/cvsDetail", produces = MediaType.APPLICATION_JSON_VALUE)
     public List<CV24> getAllCv24DetailsJson() {
         return cv24Repository.findAll();
     }*/
    @GetMapping(value = "/cvsDetail", produces = MediaType.TEXT_HTML_VALUE)
    public String getAllCv24DetailsHtml(Model model) {
        List<CV24> cv24List = cv24Repository.findAll();
        model.addAttribute("cvs", cv24List);

        for (CV24 cv : cv24List) {
            System.out.println("CV ID: " + cv.getId());
            Competence competence = cv.getCompetence();
            if (competence != null) {
                // Test supplémentaire pour charger spécifiquement la compétence
                Competence loadedCompetence = competenceRepository.findById(competence.getCompetence_id()).orElse(null);
                if (loadedCompetence != null) {
                    List<Diplome> diplomes = loadedCompetence.getDiplomes();
                    List<Certif> certifs = loadedCompetence.getCertifs();

                    System.out.println("Nombre de diplômes pour Competence ID " + loadedCompetence.getCompetence_id() + ": " + diplomes.size());
                    System.out.println("Nombre de certificats pour Competence ID " + loadedCompetence.getCompetence_id() + ": " + certifs.size());

                    System.out.println("Liste des diplômes:");
                    for (Diplome diplome : diplomes) {
                        System.out.println("\tDiplôme ID: " + diplome.getId() + ", Date: " + diplome.getDate() + ", Institut: " + diplome.getInstitut() + ", Niveau: " + diplome.getNiveau());
                    }

                    System.out.println("Liste des certificats:");
                    for (Certif certif : certifs) {
                        System.out.println("\tCertificat ID: " + certif.getId() + ", Date de début: " + certif.getDatedeb() + ", Date de fin: " + certif.getDatefin() + ", Titre: " + certif.getTitre());
                    }
                }
            } else {
                System.out.println("Aucune compétence associée pour ce CV.");
            }
            System.out.println("--------------------------------");
        }

        return "ListecvDetail";
    }

    @GetMapping("/cv/{id}")
public String showCv(@PathVariable Long id, Model model) {
    Logger logger = LoggerFactory.getLogger(getClass());

    try {
        // Chemin absolu vers le fichier XSLT
        File xsltFile = new ClassPathResource("cv24.tp4.xslt").getFile();

        // Vérifie que la ressource XSLT existe
        if (!xsltFile.exists()) {
            throw new IOException("XSLT file not found at specified path");
        }

        // Trouve le CV par son ID ou lance une exception s'il n'est pas trouvé
        CV24 cv = cv24Repository.findById(id)
                                 .orElseThrow(() -> new Exception("CV not found"));

        // Convertit l'entité CV en données XML
        String xmlData = xmlProcessorService.convertEntityToXml(cv);

        // Applique la transformation XSLT
        String htmlData = xmlProcessorService.applyXsltTransformation(xmlData, xsltFile);

        // Log le HTML résultant pour le contrôle
        logger.info("HTML Data after XSLT Transformation: {}", htmlData);

        // Ajoute le résultat HTML au modèle pour l'affichage dans la vue
        model.addAttribute("cvHtml", htmlData);

        // Retourne le nom de la vue à afficher
        return "cvDisplay"; 
    } catch (IOException e) {
        // Gère les exceptions liées au chargement du fichier XSLT
        logger.error("Error loading XSLT file: ", e);
        model.addAttribute("error", "Error loading XSLT file: " + e.getMessage());
        return "errorPage"; // Retourne le nom de la vue d'erreur
    } catch (Exception e) {
        // Gère les autres exceptions
        logger.error("Error processing CV: ", e);
        model.addAttribute("error", e.getMessage());
        return "errorPage"; // Retourne le nom de la vue d'erreur
    }
}




}