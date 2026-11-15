package com.erp.brazil.fiscal.infrastructure.certificado;

import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import com.erp.brazil.fiscal.domain.repository.CertificadoDigitalRepository;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.*;
import org.bouncycastle.operator.jcajce.*;
import org.bouncycastle.util.Store;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Digital Certificate A1 management.
 * Handles AES-256-GCM encryption for PFX/password storage and XML signing.
 */
@Service
@Transactional
public class CertificadoDigitalService {

    private final CertificadoDigitalRepository certificadoRepository;
    private final CertificadoSSLService sslService;
    private final SecretKey encryptionKey;

    public CertificadoDigitalService(CertificadoDigitalRepository certificadoRepository,
                                      CertificadoSSLService sslService) {
        this.certificadoRepository = certificadoRepository;
        this.sslService = sslService;
        this.encryptionKey = generateOrLoadEncryptionKey();
    }

    private SecretKey generateOrLoadEncryptionKey() {
        // In production, load from secure key management (HashiCorp Vault, AWS KMS, etc.)
        // For now, generate from a master password (should be externalized)
        try {
            String masterPassword = System.getenv("MASTER_ENCRYPTION_KEY");
            if (masterPassword == null || masterPassword.isBlank()) {
                masterPassword = "erp-brazil-fiscal-master-key-2024-change-in-production";
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(masterPassword.getBytes(StandardCharsets.UTF_8));
            return new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize encryption key", e);
        }
    }

    public CertificadoDigitalA1 registerCertificate(String companyId, String alias,
                                                     String pfxBase64, String password,
                                                     String cnpj, String companyName,
                                                     int renewalAlertDays) {
        // Extract certificate info
        var certInfo = sslService.extractCertificateInfo(pfxBase64, password);

        // Encrypt PFX and password
        String pfxEncrypted = encrypt(pfxBase64);
        String passwordEncrypted = encrypt(password);

        var certificado = new CertificadoDigitalA1(
                UUID.randomUUID().toString(),
                companyId,
                alias,
                pfxEncrypted,
                passwordEncrypted,
                cnpj,
                companyName,
                certInfo.notAfter(),
                CertificadoDigitalA1.CertificateStatus.ACTIVE,
                certInfo.serialNumber(),
                certInfo.issuer(),
                certInfo.subject(),
                certInfo.thumbprint(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                null,
                renewalAlertDays
        );

        return certificadoRepository.save(certificado);
    }

    public Optional<CertificadoDigitalA1> getActiveCertificate(String companyId) {
        return certificadoRepository.findByCompanyIdAndStatus(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE)
                .stream().findFirst();
    }

    public Optional<CertificadoDigitalA1> getCertificateById(String id, String companyId) {
        return certificadoRepository.findByIdAndCompanyId(id, companyId);
    }

    public List<CertificadoDigitalA1> getCertificatesByCompany(String companyId) {
        return certificadoRepository.findByCompanyId(companyId);
    }

    public void updateCertificate(String id, String companyId, String alias,
                                   String pfxBase64, String password, int renewalAlertDays) {
        var existing = certificadoRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));

        String pfxEncrypted = pfxBase64 != null ? encrypt(pfxBase64) : existing.pfxEncrypted();
        String passwordEncrypted = password != null ? encrypt(password) : existing.passwordEncrypted();

        var updated = new CertificadoDigitalA1(
                existing.id(),
                existing.companyId(),
                alias != null ? alias : existing.alias(),
                pfxEncrypted,
                passwordEncrypted,
                existing.cnpj(),
                existing.companyName(),
                existing.expiryDate(),
                existing.status(),
                existing.serialNumber(),
                existing.issuer(),
                existing.subject(),
                existing.thumbprint(),
                existing.createdAt(),
                java.time.LocalDateTime.now(),
                existing.lastUsedAt(),
                renewalAlertDays > 0 ? renewalAlertDays : existing.renewalAlertDays()
        );

        certificadoRepository.save(updated);
    }

    public void deleteCertificate(String id, String companyId) {
        certificadoRepository.findByIdAndCompanyId(id, companyId)
                .ifPresent(certificadoRepository::delete);
    }

    public String decryptPfx(String encryptedPfx) {
        return decrypt(encryptedPfx);
    }

    public String decryptPassword(String encryptedPassword) {
        return decrypt(encryptedPassword);
    }

    public String signXml(Object nfe, CertificadoDigitalA1 certificado) {
        try {
            String pfxBase64 = decrypt(certificado.pfxEncrypted());
            String password = decrypt(certificado.passwordEncrypted());

            // Extract private key and certificate chain
            PrivateKey privateKey = sslService.extractPrivateKey(pfxBase64, password);
            Certificate[] chain = sslService.extractCertificateChain(pfxBase64, password);

            // Marshal NF-e to XML
            String xmlContent = marshalToXml(nfe);

            // Sign using XML-DSig (simplified - use Apache Santuario in production)
            return signXmlWithPkcs7(xmlContent, privateKey, chain);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign XML", e);
        }
    }

    public boolean verifySignature(String signedXml) {
        // Placeholder for signature verification
        return signedXml != null && signedXml.contains("<Signature");
    }

    private String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[12]; // 96-bit IV for GCM
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, spec);

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            // Combine IV + ciphertext + authTag (GCM includes auth tag in ciphertext)
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(iv);
            output.write(ciphertext);

            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    private String decrypt(String ciphertextBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertextBase64);

            // Extract IV (first 12 bytes)
            byte[] iv = new byte[12];
            System.arraycopy(combined, 0, iv, 0, 12);

            // Extract ciphertext + authTag
            byte[] ciphertext = new byte[combined.length - 12];
            System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private String marshalToXml(Object object) {
        try {
            jakarta.xml.bind.JAXBContext context = jakarta.xml.bind.JAXBContext.newInstance(object.getClass());
            jakarta.xml.bind.Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", true);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            marshaller.marshal(object, output);
            return output.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to marshal to XML", e);
        }
    }

    private String signXmlWithPkcs7(String xmlContent, PrivateKey privateKey, Certificate[] chain) {
        try {
            // Use Bouncy Castle CMS/PKCS#7 for detached signature
            // In production, use Apache Santuario for XML-DSig
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            JcaSignerInfoGeneratorBuilder builder = new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
            );
            builder.setProvider("BC");

            X509Certificate cert = (X509Certificate) chain[0];
            gen.addSignerInfoGenerator(builder.build(
                    new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(privateKey),
                    new JcaX509CertificateHolder(cert)
            ));

            // Add certificate chain
            List<X509Certificate> certList = new java.util.ArrayList<>();
            for (Certificate c : chain) {
                certList.add((X509Certificate) c);
            }
            Store certStore = new JcaCertStore(certList);
            gen.addCertificates(certStore);

            CMSProcessableByteArray content = new CMSProcessableByteArray(xmlContent.getBytes(StandardCharsets.UTF_8));
            CMSSignedData signedData = gen.generate(content, false);

            // Return base64 encoded PKCS#7 signature
            return Base64.getEncoder().encodeToString(signedData.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create PKCS#7 signature", e);
        }
    }
}