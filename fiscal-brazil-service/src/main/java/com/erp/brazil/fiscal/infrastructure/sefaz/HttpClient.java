package com.erp.brazil.fiscal.infrastructure.sefaz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpClient {

    private final SEFAZConfig config;

    @Value("${sefaz.ssl-validation:true}")
    private boolean sslValidation;

    public String post(String url, String xml, X509Certificate clientCert) {
        log.debug("POST to SEFAZ: {}", url);

        try (CloseableHttpClient httpClient = buildHttpClient(clientCert)) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/soap+xml; charset=utf-8");
            post.setHeader("SOAPAction", "");
            post.setEntity(new StringEntity(xml, ContentType.create("text/xml", "UTF-8")));

            return httpClient.execute(post, response -> {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes());

                log.debug("SEFAZ response status: {}", statusCode);

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("SEFAZ HTTP error: " + statusCode + " - " + responseBody);
                }
            });
        } catch (Exception e) {
            log.error("Error posting to SEFAZ: {}", url, e);
            throw new RuntimeException("Failed to communicate with SEFAZ", e);
        }
    }

    private CloseableHttpClient buildHttpClient(X509Certificate clientCert) {
        try {
            SSLContextBuilder builder = SSLContextBuilder.create();

            if (!sslValidation) {
                builder.loadTrustMaterial(null, (chain, authType) -> true);
            }

            if (clientCert != null) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(null, null);
                keyStore.setCertificateEntry("client", clientCert);
                builder.loadKeyMaterial(keyStore, null);
            }

            SSLContext sslContext = builder.build();
            return HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setConnectionTimeToLive(config.getConnectTimeout(), java.util.concurrent.TimeUnit.MILLISECONDS)
                    .build();
        } catch (Exception e) {
            log.error("Error building HTTP client", e);
            throw new RuntimeException("Failed to create HTTP client", e);
        }
    }
}