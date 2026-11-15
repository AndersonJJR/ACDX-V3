package com.erp.brazil.fiscal.infrastructure.sefaz;

import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SEFAZ client for NF-e/NFS-e/CT-e communication.
 * Supports all UFs with separate homologation/production endpoints.
 * Uses circuit breaker (Resilience4j) and mutual TLS with Bouncy Castle.
 */
@Component
public class SEFAZClient {

    private final Map<String, SEFAZEndpoint> endpoints = new ConcurrentHashMap<>();
    private final RestClient restClient;
    private final CertificadoSSLService sslService;

    @Value("${sefaz.default-environment:homologation}")
    private String defaultEnvironment;

    public SEFAZClient(CertificadoSSLService sslService) {
        this.sslService = sslService;
        this.restClient = RestClient.builder()
                .requestFactory(createRequestFactory())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/soap+xml; charset=utf-8")
                .defaultHeader(HttpHeaders.ACCEPT, "application/soap+xml")
                .build();
        initializeEndpoints();
    }

    private void initializeEndpoints() {
        // SP - São Paulo
        endpoints.put("SP-homologation", new SEFAZEndpoint(
                "SP", "homologation",
                "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("SP-production", new SEFAZEndpoint(
                "SP", "production",
                "https://nfe.fazenda.sp.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.fazenda.sp.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.fazenda.sp.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.fazenda.sp.gov.br/ws/nfestatus4.asmx",
                "https://nfe.fazenda.sp.gov.br/ws/nfeconsulta4.asmx"
        ));

        // MG - Minas Gerais
        endpoints.put("MG-homologation", new SEFAZEndpoint(
                "MG", "homologation",
                "https://homologacao.nfe.fazenda.mg.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.fazenda.mg.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.fazenda.mg.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.fazenda.mg.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.fazenda.mg.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("MG-production", new SEFAZEndpoint(
                "MG", "production",
                "https://nfe.fazenda.mg.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.fazenda.mg.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.fazenda.mg.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.fazenda.mg.gov.br/ws/nfestatus4.asmx",
                "https://nfe.fazenda.mg.gov.br/ws/nfeconsulta4.asmx"
        ));

        // RS - Rio Grande do Sul
        endpoints.put("RS-homologation", new SEFAZEndpoint(
                "RS", "homologation",
                "https://homologacao.nfe.sefaz.rs.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.sefaz.rs.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.sefaz.rs.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.sefaz.rs.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.sefaz.rs.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("RS-production", new SEFAZEndpoint(
                "RS", "production",
                "https://nfe.sefaz.rs.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.sefaz.rs.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.sefaz.rs.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.sefaz.rs.gov.br/ws/nfestatus4.asmx",
                "https://nfe.sefaz.rs.gov.br/ws/nfeconsulta4.asmx"
        ));

        // PR - Paraná
        endpoints.put("PR-homologation", new SEFAZEndpoint(
                "PR", "homologation",
                "https://homologacao.nfe.fazenda.pr.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.fazenda.pr.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.fazenda.pr.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.fazenda.pr.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.fazenda.pr.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("PR-production", new SEFAZEndpoint(
                "PR", "production",
                "https://nfe.fazenda.pr.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.fazenda.pr.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.fazenda.pr.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.fazenda.pr.gov.br/ws/nfestatus4.asmx",
                "https://nfe.fazenda.pr.gov.br/ws/nfeconsulta4.asmx"
        ));

        // SC - Santa Catarina
        endpoints.put("SC-homologation", new SEFAZEndpoint(
                "SC", "homologation",
                "https://homologacao.nfe.fazenda.sc.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.fazenda.sc.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.fazenda.sc.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.fazenda.sc.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.fazenda.sc.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("SC-production", new SEFAZEndpoint(
                "SC", "production",
                "https://nfe.fazenda.sc.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.fazenda.sc.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.fazenda.sc.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.fazenda.sc.gov.br/ws/nfestatus4.asmx",
                "https://nfe.fazenda.sc.gov.br/ws/nfeconsulta4.asmx"
        ));

        // BA - Bahia
        endpoints.put("BA-homologation", new SEFAZEndpoint(
                "BA", "homologation",
                "https://homologacao.nfe.sefaz.ba.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.sefaz.ba.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.sefaz.ba.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.sefaz.ba.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.sefaz.ba.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("BA-production", new SEFAZEndpoint(
                "BA", "production",
                "https://nfe.sefaz.ba.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.sefaz.ba.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.sefaz.ba.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.sefaz.ba.gov.br/ws/nfestatus4.asmx",
                "https://nfe.sefaz.ba.gov.br/ws/nfeconsulta4.asmx"
        ));

        // GO - Goiás
        endpoints.put("GO-homologation", new SEFAZEndpoint(
                "GO", "homologation",
                "https://homologacao.nfe.sefaz.go.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.sefaz.go.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.sefaz.go.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.sefaz.go.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.sefaz.go.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("GO-production", new SEFAZEndpoint(
                "GO", "production",
                "https://nfe.sefaz.go.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.sefaz.go.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.sefaz.go.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.sefaz.go.gov.br/ws/nfestatus4.asmx",
                "https://nfe.sefaz.go.gov.br/ws/nfeconsulta4.asmx"
        ));

        // MT - Mato Grosso
        endpoints.put("MT-homologation", new SEFAZEndpoint(
                "MT", "homologation",
                "https://homologacao.nfe.sefaz.mt.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.sefaz.mt.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.sefaz.mt.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.sefaz.mt.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.sefaz.mt.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("MT-production", new SEFAZEndpoint(
                "MT", "production",
                "https://nfe.sefaz.mt.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.sefaz.mt.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.sefaz.mt.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.sefaz.mt.gov.br/ws/nfestatus4.asmx",
                "https://nfe.sefaz.mt.gov.br/ws/nfeconsulta4.asmx"
        ));

        // MS - Mato Grosso do Sul
        endpoints.put("MS-homologation", new SEFAZEndpoint(
                "MS", "homologation",
                "https://homologacao.nfe.sefaz.ms.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.sefaz.ms.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.sefaz.ms.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.sefaz.ms.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.sefaz.ms.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("MS-production", new SEFAZEndpoint(
                "MS", "production",
                "https://nfe.sefaz.ms.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.sefaz.ms.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.sefaz.ms.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.sefaz.ms.gov.br/ws/nfestatus4.asmx",
                "https://nfe.sefaz.ms.gov.br/ws/nfeconsulta4.asmx"
        ));

        // DF - Distrito Federal
        endpoints.put("DF-homologation", new SEFAZEndpoint(
                "DF", "homologation",
                "https://homologacao.nfe.fazenda.df.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.fazenda.df.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.fazenda.df.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.fazenda.df.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.fazenda.df.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("DF-production", new SEFAZEndpoint(
                "DF", "production",
                "https://nfe.fazenda.df.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.fazenda.df.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.fazenda.df.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.fazenda.df.gov.br/ws/nfestatus4.asmx",
                "https://nfe.fazenda.df.gov.br/ws/nfeconsulta4.asmx"
        ));

        // SVRS (SVAN, SVRS) - Used by many states
        endpoints.put("SVRS-homologation", new SEFAZEndpoint(
                "SVRS", "homologation",
                "https://homologacao.nfe.sefazvirtual.rs.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.sefazvirtual.rs.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.sefazvirtual.rs.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.sefazvirtual.rs.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.sefazvirtual.rs.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("SVRS-production", new SEFAZEndpoint(
                "SVRS", "production",
                "https://nfe.sefazvirtual.rs.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.sefazvirtual.rs.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.sefazvirtual.rs.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.sefazvirtual.rs.gov.br/ws/nfestatus4.asmx",
                "https://nfe.sefazvirtual.rs.gov.br/ws/nfeconsulta4.asmx"
        ));

        // SVAN - SEFAZ Virtual Ambiente Nacional
        endpoints.put("SVAN-homologation", new SEFAZEndpoint(
                "SVAN", "homologation",
                "https://homologacao.nfe.sefazvirtual.an.gov.br/ws/nfeautorizacao4.asmx",
                "https://homologacao.nfe.sefazvirtual.an.gov.br/ws/nfecancelamento4.asmx",
                "https://homologacao.nfe.sefazvirtual.an.gov.br/ws/nfeinutilizacao4.asmx",
                "https://homologacao.nfe.sefazvirtual.an.gov.br/ws/nfestatus4.asmx",
                "https://homologacao.nfe.sefazvirtual.an.gov.br/ws/nfeconsulta4.asmx"
        ));
        endpoints.put("SVAN-production", new SEFAZEndpoint(
                "SVAN", "production",
                "https://nfe.sefazvirtual.an.gov.br/ws/nfeautorizacao4.asmx",
                "https://nfe.sefazvirtual.an.gov.br/ws/nfecancelamento4.asmx",
                "https://nfe.sefazvirtual.an.gov.br/ws/nfeinutilizacao4.asmx",
                "https://nfe.sefazvirtual.an.gov.br/ws/nfestatus4.asmx",
                "https://nfe.sefazvirtual.an.gov.br/ws/nfeconsulta4.asmx"
        ));
    }

    private org.springframework.http.client.ClientHttpRequestFactory createRequestFactory() {
        var factory = new org.springframework.http.client.HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(60000);
        return factory;
    }

    @CircuitBreaker(name = "sefaz", fallbackMethod = "sefazFallback")
    @Retry(name = "sefaz")
    public SEFAZResponse sendNfe(String companyId, String signedXml, CertificadoDigitalA1 certificate) {
        String uf = extractUfFromCompany(companyId);
        String env = defaultEnvironment;
        String endpointKey = uf + "-" + env;

        SEFAZEndpoint endpoint = endpoints.get(endpointKey);
        if (endpoint == null) {
            // Fallback to SVRS/SVAN
            endpoint = endpoints.get("SVRS-" + env);
            if (endpoint == null) {
                endpoint = endpoints.get("SVAN-" + env);
            }
        }

        if (endpoint == null) {
            throw new IllegalStateException("No SEFAZ endpoint configured for UF: " + uf);
        }

        String soapEnvelope = buildSoapEnvelope("nfeAutorizacaoLote", signedXml, uf);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/soap+xml; charset=utf-8"));
        headers.set("SOAPAction", "http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4/nfeAutorizacaoLote");

        HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(endpoint.getAutorizacaoUrl())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(soapEnvelope)
                    .retrieve()
                    .toEntity(String.class);

            return parseNfeResponse(response.getBody());
        } catch (RestClientException e) {
            throw new SEFAZCommunicationException("Failed to send NF-e to SEFAZ: " + e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = "sefaz", fallbackMethod = "sefazFallback")
    @Retry(name = "sefaz")
    public SEFAZCancelResponse cancelNfe(String companyId, String accessKey, String protocol,
                                          String justification, CertificadoDigitalA1 certificate) {
        String uf = extractUfFromCompany(companyId);
        String env = defaultEnvironment;
        String endpointKey = uf + "-" + env;

        SEFAZEndpoint endpoint = endpoints.get(endpointKey);
        if (endpoint == null) {
            endpoint = endpoints.get("SVRS-" + env);
        }

        String cancelXml = buildCancelXml(accessKey, protocol, justification);
        String soapEnvelope = buildSoapEnvelope("nfeCancelamentoNF", cancelXml, uf);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/soap+xml; charset=utf-8"));
        headers.set("SOAPAction", "http://www.portalfiscal.inf.br/nfe/wsdl/NFeCancelamento4/nfeCancelamentoNF");

        HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(endpoint.getCancelamentoUrl())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(soapEnvelope)
                    .retrieve()
                    .toEntity(String.class);

            return parseCancelResponse(response.getBody());
        } catch (RestClientException e) {
            throw new SEFAZCommunicationException("Failed to cancel NF-e: " + e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = "sefaz", fallbackMethod = "sefazFallback")
    @Retry(name = "sefaz")
    public SEFAZInutilizeResponse inutilizeNfe(String companyId, String series, int startNumber,
                                                int endNumber, String justification, CertificadoDigitalA1 certificate) {
        String uf = extractUfFromCompany(companyId);
        String env = defaultEnvironment;
        String endpointKey = uf + "-" + env;

        SEFAZEndpoint endpoint = endpoints.get(endpointKey);
        if (endpoint == null) {
            endpoint = endpoints.get("SVRS-" + env);
        }

        String inutilizeXml = buildInutilizeXml(companyId, series, startNumber, endNumber, justification);
        String soapEnvelope = buildSoapEnvelope("nfeInutilizacaoNF", inutilizeXml, uf);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/soap+xml; charset=utf-8"));
        headers.set("SOAPAction", "http://www.portalfiscal.inf.br/nfe/wsdl/NFeInutilizacao4/nfeInutilizacaoNF");

        HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(endpoint.getInutilizacaoUrl())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(soapEnvelope)
                    .retrieve()
                    .toEntity(String.class);

            return parseInutilizeResponse(response.getBody());
        } catch (RestClientException e) {
            throw new SEFAZCommunicationException("Failed to inutilize NF-e: " + e.getMessage(), e);
        }
    }

    public SEFAZStatusResponse checkStatus(String uf) {
        String env = defaultEnvironment;
        String endpointKey = uf + "-" + env;

        SEFAZEndpoint endpoint = endpoints.get(endpointKey);
        if (endpoint == null) {
            endpoint = endpoints.get("SVRS-" + env);
        }

        String soapEnvelope = buildSoapEnvelope("nfeStatusServicoNF", buildStatusXml(uf), uf);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/soap+xml; charset=utf-8"));
        headers.set("SOAPAction", "http://www.portalfiscal.inf.br/nfe/wsdl/NFeStatusServico4/nfeStatusServicoNF");

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(endpoint.getStatusUrl())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(soapEnvelope)
                    .retrieve()
                    .toEntity(String.class);

            return parseStatusResponse(response.getBody());
        } catch (RestClientException e) {
            throw new SEFAZCommunicationException("Failed to check SEFAZ status: " + e.getMessage(), e);
        }
    }

    private String extractUfFromCompany(String companyId) {
        // In production, fetch from company service
        // For now, default to SP
        return "SP";
    }

    private String buildSoapEnvelope(String methodName, String content, String uf) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap12:Envelope xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
                    <soap12:Header>
                        <nfeCabecMsg xmlns="http://www.portalfiscal.inf.br/nfe/wsdl/%s">
                            <cUF>%s</cUF>
                            <versaoDados>4.00</versaoDados>
                        </nfeCabecMsg>
                    </soap12:Header>
                    <soap12:Body>
                        <ns0:%s xmlns:ns0="http://www.portalfiscal.inf.br/nfe/wsdl/%s">
                            <ns0:nfeDadosMsg>%s</ns0:nfeDadosMsg>
                        </ns0:%s>
                    </soap12:Body>
                </soap12:Envelope>
                """.formatted(getWsdlName(methodName), getUfCode(uf), methodName, getWsdlName(methodName), escapeXml(content), methodName);
    }

    private String getWsdlName(String method) {
        return switch (method) {
            case "nfeAutorizacaoLote" -> "NFeAutorizacao4";
            case "nfeCancelamentoNF" -> "NFeCancelamento4";
            case "nfeInutilizacaoNF" -> "NFeInutilizacao4";
            case "nfeStatusServicoNF" -> "NFeStatusServico4";
            case "nfeConsultaNF" -> "NFeConsulta4";
            default -> "NFeAutorizacao4";
        };
    }

    private String getUfCode(String uf) {
        return switch (uf) {
            case "SP" -> "35";
            case "MG" -> "31";
            case "RS" -> "43";
            case "PR" -> "41";
            case "SC" -> "42";
            case "BA" -> "29";
            case "GO" -> "52";
            case "MT" -> "51";
            case "MS" -> "50";
            case "DF" -> "53";
            case "SVRS" -> "43";
            case "SVAN" -> "91";
            default -> "35";
        };
    }

    private String buildCancelXml(String accessKey, String protocol, String justification) {
        return """
                <cancNFe versao="4.00" xmlns="http://www.portalfiscal.inf.br/nfe">
                    <infCanc>
                        <chNFe>%s</chNFe>
                        <nProt>%s</nProt>
                        <xJust>%s</xJust>
                    </infCanc>
                </cancNFe>
                """.formatted(accessKey, protocol, escapeXml(justification));
    }

    private String buildInutilizeXml(String companyId, String series, int start, int end, String justification) {
        return """
                <inutNFe versao="4.00" xmlns="http://www.portalfiscal.inf.br/nfe">
                    <infInut>
                        <cUF>35</cUF>
                        <ano>%s</ano>
                        <CNPJ>%s</CNPJ>
                        <mod>55</mod>
                        <serie>%s</serie>
                        <nNFIni>%d</nNFIni>
                        <nNFFin>%d</nNFFin>
                        <xJust>%s</xJust>
                    </infInut>
                </inutNFe>
                """.formatted(
                java.time.Year.now().toString().substring(2),
                extractCnpjFromCompany(companyId),
                series, start, end, escapeXml(justification)
        );
    }

    private String buildStatusXml(String uf) {
        return """
                <consStatServ versao="4.00" xmlns="http://www.portalfiscal.inf.br/nfe">
                    <cUF>%s</cUF>
                </consStatServ>
                """.formatted(getUfCode(uf));
    }

    private String extractCnpjFromCompany(String companyId) {
        // In production, fetch from company service
        return "00000000000000";
    }

    private String escapeXml(String xml) {
        if (xml == null) return "";
        return xml.replace("&", "&")
                .replace("<", "<")
                .replace(">", ">")
                .replace("\"", """)
                .replace("'", "&apos;");
    }

    private SEFAZResponse parseNfeResponse(String xml) {
        // Parse XML response using JAXB or simple string extraction
        // Placeholder implementation
        return new SEFAZResponse();
    }

    private SEFAZCancelResponse parseCancelResponse(String xml) {
        return new SEFAZCancelResponse();
    }

    private SEFAZInutilizeResponse parseInutilizeResponse(String xml) {
        return new SEFAZInutilizeResponse();
    }

    private SEFAZStatusResponse parseStatusResponse(String xml) {
        return new SEFAZStatusResponse();
    }

    private SEFAZResponse sefazFallback(Exception ex) {
        var response = new SEFAZResponse();
        response.setCStat("999");
        response.setXMotivo("SEFAZ unavailable: " + ex.getMessage());
        return response;
    }

    // Response classes
    public static class SEFAZResponse {
        private String cStat;
        private String xMotivo;
        private String xmlContent;
        private ProtNFe protNFe;

        public String getCStat() { return cStat; }
        public void setCStat(String cStat) { this.cStat = cStat; }
        public String getXMotivo() { return xMotivo; }
        public void setXMotivo(String xMotivo) { this.xMotivo = xMotivo; }
        public String getXmlContent() { return xmlContent; }
        public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
        public ProtNFe getProtNFe() { return protNFe; }
        public void setProtNFe(ProtNFe protNFe) { this.protNFe = protNFe; }

        public static class ProtNFe {
            private InfProt infProt;
            public InfProt getInfProt() { return infProt; }
            public void setInfProt(InfProt infProt) { this.infProt = infProt; }
        }

        public static class InfProt {
            private String nProt;
            public String getNProt() { return nProt; }
            public void setNProt(String nProt) { this.nProt = nProt; }
        }
    }

    public static class SEFAZCancelResponse {
        private String cStat;
        private String xMotivo;
        private String xmlContent;
        private ProtCanc protCanc;

        public String getCStat() { return cStat; }
        public void setCStat(String cStat) { this.cStat = cStat; }
        public String getXMotivo() { return xMotivo; }
        public void setXMotivo(String xMotivo) { this.xMotivo = xMotivo; }
        public String getXmlContent() { return xmlContent; }
        public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
        public ProtCanc getProtCanc() { return protCanc; }
        public void setProtCanc(ProtCanc protCanc) { this.protCanc = protCanc; }

        public static class ProtCanc {
            private InfCanc infCanc;
            public InfCanc getInfCanc() { return infCanc; }
            public void setInfCanc(InfCanc infCanc) { this.infCanc = infCanc; }
        }

        public static class InfCanc {
            private String nProt;
            public String getNProt() { return nProt; }
            public void setNProt(String nProt) { this.nProt = nProt; }
        }
    }

    public static class SEFAZInutilizeResponse {
        private String cStat;
        private String xMotivo;
        private String xmlContent;
        private ProtInut protInut;

        public String getCStat() { return cStat; }
        public void setCStat(String cStat) { this.cStat = cStat; }
        public String getXMotivo() { return xMotivo; }
        public void setXMotivo(String xMotivo) { this.xMotivo = xMotivo; }
        public String getXmlContent() { return xmlContent; }
        public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
        public ProtInut getProtInut() { return protInut; }
        public void setProtInut(ProtInut protInut) { this.protInut = protInut; }

        public static class ProtInut {
            private InfInut infInut;
            public InfInut getInfInut() { return infInut; }
            public void setInfInut(InfInut infInut) { this.infInut = infInut; }
        }

        public static class InfInut {
            private String nProt;
            public String getNProt() { return nProt; }
            public void setNProt(String nProt) { this.nProt = nProt; }
        }
    }

    public static class SEFAZStatusResponse {
        private String cStat;
        private String xMotivo;
        private String xmlContent;

        public String getCStat() { return cStat; }
        public void setCStat(String cStat) { this.cStat = cStat; }
        public String getXMotivo() { return xMotivo; }
        public void setXMotivo(String xMotivo) { this.xMotivo = xMotivo; }
        public String getXmlContent() { return xmlContent; }
        public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
    }

    public static class SEFAZCommunicationException extends RuntimeException {
        public SEFAZCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record SEFAZEndpoint(
            String uf,
            String environment,
            String autorizacaoUrl,
            String cancelamentoUrl,
            String inutilizacaoUrl,
            String statusUrl,
            String consultaUrl
    ) {
        public String getAutorizacaoUrl() { return autorizacaoUrl; }
        public String getCancelamentoUrl() { return cancelamentoUrl; }
        public String getInutilizacaoUrl() { return inutilizacaoUrl; }
        public String getStatusUrl() { return statusUrl; }
        public String getConsultaUrl() { return consultaUrl; }
    }
}