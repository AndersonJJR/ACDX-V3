package com.erp.brazil.fiscal.infrastructure.leis;

import com.erp.brazil.fiscal.domain.model.CEST;
import com.erp.brazil.fiscal.domain.model.CFOP;
import com.erp.brazil.fiscal.domain.model.NCM;
import com.erp.brazil.fiscal.domain.repository.CESTRepository;
import com.erp.brazil.fiscal.domain.repository.CFOPRepository;
import com.erp.brazil.fiscal.domain.repository.NCMRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeisSyncJob {

    private final NCMRepository ncmRepository;
    private final CESTRepository cestRepository;
    private final CFOPRepository cfopRepository;

    @Value("${leis.sync.enabled:true}")
    private boolean enabled;

    @Value("${leis.sync.repo-url:https://raw.githubusercontent.com/ctresb/leis/main}")
    private String repoUrl;

    private final RestClient restClient = RestClient.create();

    @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
    @Transactional
    public void syncAll() {
        if (!enabled) {
            log.info("Leis sync is disabled");
            return;
        }

        log.info("Starting leis synchronization from {}", repoUrl);

        try {
            syncNCM();
            syncCEST();
            syncCFOP();
            log.info("Leis synchronization completed successfully");
        } catch (Exception e) {
            log.error("Error during leis synchronization", e);
        }
    }

    private void syncNCM() {
        log.info("Syncing NCM...");

        String url = repoUrl + "/ncm/ncm.json";
        List<Map<String, Object>> data = restClient.get()
                .uri(url)
                .retrieve()
                .body(List.class);

        if (data == null) {
            log.warn("No NCM data received");
            return;
        }

        int created = 0, updated = 0;
        for (Map<String, Object> item : data) {
            String code = (String) item.get("codigo");
            if (code == null) continue;

            NCM ncm = ncmRepository.findById(code).orElse(NCM.builder().code(code).build());
            ncm.setDescription((String) item.get("descricao"));
            ncm.setChapter((String) item.get("capitulo"));
            ncm.setPosition((String) item.get("posicao"));
            ncm.setSubposition((String) item.get("subposicao"));
            ncm.setItem((String) item.get("item"));
            ncm.setActive(true);
            ncm.setVersion((String) item.get("versao"));
            ncm.setLastSync(LocalDateTime.now());

            if (ncmRepository.existsByCode(code)) {
                updated++;
            } else {
                created++;
            }
            ncmRepository.save(ncm);
        }

        log.info("NCM sync completed: {} created, {} updated", created, updated);
    }

    private void syncCEST() {
        log.info("Syncing CEST...");

        String url = repoUrl + "/cest/cest.json";
        List<Map<String, Object>> data = restClient.get()
                .uri(url)
                .retrieve()
                .body(List.class);

        if (data == null) {
            log.warn("No CEST data received");
            return;
        }

        int created = 0, updated = 0;
        for (Map<String, Object> item : data) {
            String code = (String) item.get("codigo");
            if (code == null) continue;

            CEST cest = cestRepository.findById(code).orElse(CEST.builder().code(code).build());
            cest.setDescription((String) item.get("descricao"));
            cest.setSegment((String) item.get("segmento"));
            cest.setNcmCode((String) item.get("ncm"));
            cest.setActive(true);
            cest.setVersion((String) item.get("versao"));
            cest.setLastSync(LocalDateTime.now());

            if (cestRepository.existsByCode(code)) {
                updated++;
            } else {
                created++;
            }
            cestRepository.save(cest);
        }

        log.info("CEST sync completed: {} created, {} updated", created, updated);
    }

    private void syncCFOP() {
        log.info("Syncing CFOP...");

        String url = repoUrl + "/cfop/cfop.json";
        List<Map<String, Object>> data = restClient.get()
                .uri(url)
                .retrieve()
                .body(List.class);

        if (data == null) {
            log.warn("No CFOP data received");
            return;
        }

        int created = 0, updated = 0;
        for (Map<String, Object> item : data) {
            String code = (String) item.get("codigo");
            if (code == null) continue;

            CFOP cfop = cfopRepository.findById(code).orElse(CFOP.builder().code(code).build());
            cfop.setDescription((String) item.get("descricao"));
            cfop.setType((String) item.get("tipo"));
            cfop.setCategory((String) item.get("categoria"));
            cfop.setGeneratesIcms(Boolean.TRUE.equals(item.get("geraIcms")));
            cfop.setGeneratesIpi(Boolean.TRUE.equals(item.get("geraIpi")));
            cfop.setGeneratesPisCofins(Boolean.TRUE.equals(item.get("geraPisCofins")));
            cfop.setActive(true);
            cfop.setVersion((String) item.get("versao"));
            cfop.setLastSync(LocalDateTime.now());

            if (cfopRepository.existsByCode(code)) {
                updated++;
            } else {
                created++;
            }
            cfopRepository.save(cfop);
        }

        log.info("CFOP sync completed: {} created, {} updated", created, updated);
    }

    // Manual trigger endpoints
    public void syncNCMManual() {
        syncNCM();
    }

    public void syncCESTManual() {
        syncCEST();
    }

    public void syncCFOPManual() {
        syncCFOP();
    }
}