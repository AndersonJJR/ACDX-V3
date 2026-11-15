package com.erp.brazil.fiscal.infrastructure.sefaz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class XMLSigner {

    static {
        Init.init();
    }

    public String sign(String xml, PrivateKey privateKey, X509Certificate certificate) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

            // Find the element to sign (usually <infNFe> or <infCanc> etc.)
            Element elementToSign = findElementToSign(doc);
            if (elementToSign == null) {
                throw new IllegalArgumentException("No element found to sign (expected Id attribute)");
            }

            String id = elementToSign.getAttribute("Id");
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("Element to sign must have an Id attribute");
            }

            // Create XMLSignature
            XMLSignature signature = new XMLSignature(doc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
            elementToSign.getParentNode().insertBefore(signature.getElement(), elementToSign);

            // Add reference
            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            transforms.addTransform(Transforms.TRANSFORM_C14N_OMIT_COMMENTS);
            signature.addDocument("#" + id, transforms, Constants.ALGO_ID_DIGEST_SHA256);

            // Add KeyInfo with X509Data
            signature.addKeyInfo(certificate);
            signature.addKeyInfo(certificate.getPublicKey());

            // Sign
            signature.sign(privateKey);

            // Output signed XML
            StringWriter writer = new StringWriter();
            XMLUtils.outputDOMc14nWithComments(doc, writer);
            return writer.toString();

        } catch (Exception e) {
            log.error("Error signing XML", e);
            throw new RuntimeException("Failed to sign XML", e);
        }
    }

    private Element findElementToSign(Document doc) {
        // Look for element with Id attribute (infNFe, infCanc, infInut, etc.)
        return (Element) doc.getElementsByTagName("*").item(0);
    }

    public String extractSignatureValue(String signedXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(signedXml.getBytes("UTF-8")));

            org.w3c.dom.NodeList signatureNodes = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "SignatureValue");
            if (signatureNodes.getLength() > 0) {
                return signatureNodes.item(0).getTextContent();
            }
        } catch (Exception e) {
            log.error("Error extracting signature value", e);
        }
        return null;
    }

    public X509Certificate extractCertificate(String signedXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(signedXml.getBytes("UTF-8")));

            org.w3c.dom.NodeList x509DataNodes = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "X509Data");
            if (x509DataNodes.getLength() > 0) {
                Element x509Data = (Element) x509DataNodes.item(0);
                org.w3c.dom.NodeList certNodes = x509Data.getElementsByTagNameNS(Constants.SignatureSpecNS, "X509Certificate");
                if (certNodes.getLength() > 0) {
                    String certBase64 = certNodes.item(0).getTextContent();
                    byte[] certBytes = Base64.getDecoder().decode(certBase64);
                    return (X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
                            .generateCertificate(new ByteArrayInputStream(certBytes));
                }
            }
        } catch (Exception e) {
            log.error("Error extracting certificate from XML", e);
        }
        return null;
    }
}