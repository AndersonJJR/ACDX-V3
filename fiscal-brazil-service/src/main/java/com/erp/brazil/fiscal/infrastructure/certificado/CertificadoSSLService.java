package com.erp.brazil.fiscal.infrastructure.certificado;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;

/**
 * Service for SSL/TLS configuration with client certificates (PFX/A1).
 * Uses Bouncy Castle for PKCS#12 support.
 */
@Component
public class CertificadoSSLService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public SSLContext createSslContext(String pfxBase64, String password) throws Exception {
        byte[] pfxBytes = Base64.getDecoder().decode(pfxBase64);

        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(new ByteArrayInputStream(pfxBytes), password.toCharArray());

        // Find the private key entry
        String alias = findPrivateKeyAlias(keyStore);
        if (alias == null) {
            throw new IllegalStateException("No private key found in PFX");
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm(), "BC");
        kmf.init(keyStore, password.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm(), "BC");
        tmf.init((KeyStore) null); // Use system trust store

        SSLContext sslContext = SSLContext.getInstance("TLS", "BC");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    public X509Certificate extractCertificate(String pfxBase64, String password) throws Exception {
        byte[] pfxBytes = Base64.getDecoder().decode(pfxBase64);

        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(new ByteArrayInputStream(pfxBytes), password.toCharArray());

        String alias = findPrivateKeyAlias(keyStore);
        if (alias == null) {
            throw new IllegalStateException("No private key found in PFX");
        }

        Certificate cert = keyStore.getCertificate(alias);
        if (!(cert instanceof X509Certificate)) {
            throw new IllegalStateException("Certificate is not X.509");
        }

        return (X509Certificate) cert;
    }

    public PrivateKey extractPrivateKey(String pfxBase64, String password) throws Exception {
        byte[] pfxBytes = Base64.getDecoder().decode(pfxBase64);

        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(new ByteArrayInputStream(pfxBytes), password.toCharArray());

        String alias = findPrivateKeyAlias(keyStore);
        if (alias == null) {
            throw new IllegalStateException("No private key found in PFX");
        }

        return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    }

    public Certificate[] extractCertificateChain(String pfxBase64, String password) throws Exception {
        byte[] pfxBytes = Base64.getDecoder().decode(pfxBase64);

        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(new ByteArrayInputStream(pfxBytes), password.toCharArray());

        String alias = findPrivateKeyAlias(keyStore);
        if (alias == null) {
            throw new IllegalStateException("No private key found in PFX");
        }

        return keyStore.getCertificateChain(alias);
    }

    private String findPrivateKeyAlias(KeyStore keyStore) throws KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                return alias;
            }
        }
        return null;
    }

    public record CertificateInfo(
            String subject,
            String issuer,
            String serialNumber,
            String thumbprint,
            java.time.LocalDate notBefore,
            java.time.LocalDate notAfter,
            String cnPj
    ) {
        public static CertificateInfo fromX509(X509Certificate cert) {
            String subject = cert.getSubjectX500Principal().getName();
            String issuer = cert.getIssuerX500Principal().getName();
            String serialNumber = cert.getSerialNumber().toString(16).toUpperCase();
            String thumbprint = calculateThumbprint(cert);
            java.time.LocalDate notBefore = cert.getNotBefore().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate notAfter = cert.getNotAfter().toInstant()
                    .atZone(java.time.ZoneId.ZoneId.systemDefault()).toLocalDate();

            // Extract CNPJ from subject
            String cnpj = extractCnpj(subject);

            return new CertificateInfo(subject, issuer, serialNumber, thumbprint, notBefore, notAfter, cnpj);
        }

        private static String calculateThumbprint(X509Certificate cert) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
                byte[] digest = md.digest(cert.getEncoded());
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02X", b));
                }
                return sb.toString();
            } catch (Exception e) {
                return "ERROR";
            }
        }

        private static String extractCnpj(String subject) {
            // Subject format: CN=Company Name, OU=..., O=..., C=BR, SERIALNUMBER=CNPJ 00.000.000/0000-00
            int idx = subject.indexOf("SERIALNUMBER=");
            if (idx >= 0) {
                int end = subject.indexOf(",", idx);
                if (end < 0) end = subject.length();
                String serial = subject.substring(idx + 11, end).trim();
                if (serial.startsWith("CNPJ ")) {
                    return serial.substring(5).replaceAll("[^0-9]", "");
                }
            }
            return null;
        }
    }
}