package fr.univrouen.ProjetXML.services;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
@Service
public class XmlValidationService {

    public boolean validateXml(String xmlData, String xsdFilePath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdFilePath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlData)));
            return true;
        } catch (SAXException | IOException e) {
            System.out.println("Validation error: " + e.getMessage());
            return false;
        }
    }

}
