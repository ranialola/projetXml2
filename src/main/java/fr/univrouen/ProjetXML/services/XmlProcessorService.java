package fr.univrouen.ProjetXML.services;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import fr.univrouen.ProjetXML.entities.*;
import fr.univrouen.ProjetXML.repository.*;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.*;
import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;

@Service
public class XmlProcessorService {

    @Autowired
    private DiplomeRepository diplomeRepository;
    @Autowired
    private CompetenceRepository competenceRepository;
    @Autowired
    private ProfRepository profRepository ;
    @Autowired
    private CertifRepository certifRepository;

    Logger log = LoggerFactory.getLogger(getClass());

//    public <T> T deserializeXmlToObj(String xmlData, Class<T> clazz) throws JAXBException {
//        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
//        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//
//        StringReader reader = new StringReader(xmlData);
//        return clazz.cast(unmarshaller.unmarshal(reader));
//    }

    public CV24 convertXmlToEntity(String xmlData) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CV24.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xmlData);
        CV24 cv24 = (CV24) jaxbUnmarshaller.unmarshal(reader);
        processDiplomes(cv24);
        processCertifs(cv24);
        processDetails(cv24);
        return cv24;
    }


   @Transactional
    public String convertEntityToXml(CV24 cv) {
        StringWriter writer = new StringWriter();
        if (cv == null) {
            log.error("Tentative de marshalling d'une entité CV24 null.");
            throw new IllegalArgumentException("L'entité CV24 fournie est null.");
        }

        try {
            // Création du contexte JAXB
            JAXBContext context = JAXBContext.newInstance(CV24.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Supprimer les identifiants des éléments
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            // Définir les espaces de noms et les préfixes
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
                @Override
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    if ("http://univ.fr/cv24".equals(namespaceUri)) {
                        return "cv24";
                    }
                    return suggestion;
                }
            });

            // Générer le XML pour le reste du CV24
            JAXBElement<CV24> jaxbElement = new JAXBElement<>(new QName("http://univ.fr/cv24", "cv24"), CV24.class, cv);
            marshaller.marshal(jaxbElement, writer);

            // Récupérer le XML généré
            String xmlString = writer.toString();
            System.out.println(xmlString);

            return xmlString;
        } catch (JAXBException e) {
            log.error("Erreur lors du marshalling de l'entité CV24 : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la conversion de l'entité en XML", e);
        }
    }

    public String applyXsltTransformation(String xmlData, File xsltFile) throws TransformerException, IOException {
        Source xmlSource = new StreamSource(new StringReader(xmlData));
        Source xsltSource = new StreamSource(xsltFile);

        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);

        // Utilisation de la TransformerFactory de Saxon
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xsltSource);

        // Applique la transformation XSLT et stocke le résultat dans sw
        transformer.transform(xmlSource, result);

        return sw.toString();
    }




    private void processDiplomes(CV24 cv24) {
        for (Diplome diplome : cv24.getCompetence().getDiplomes()) {
            if (diplome.getCompetence() == null) {
                Competence competence = new Competence();
                competence = competenceRepository.save(competence);
                diplome.setCompetence(competence);
            }
        }
    }
    public void processDetails(CV24 cv24) {
        for (Detail detail : cv24.getProf().getDetails()) {
            if (detail.getProf() == null) {
                Prof prof= new Prof();
                prof= profRepository.save(prof);
                detail.setProf(prof);
            }
        }
    }

    private void processCertifs(CV24 cv24) {
        for (Certif certif : cv24.getCompetence().getCertifs()) {
            if (certif.getCompetence() == null) {
                Competence competence = new Competence();
                competence = competenceRepository.save(competence);
                certif.setCompetence(competence);
            }
        }
    }
}