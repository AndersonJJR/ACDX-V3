package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.CTE;
import com.erp.brazil.fiscal.domain.model.CTEStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CTERepository extends JpaRepository<CTE, String> {
    Optional<CTE> findByChaveAcesso(String chaveAcesso);

    List<CTE> findByEmitenteCnpjAndStatusIn(String emitenteCnpj, List<CTEStatus> statuses);

    List<CTE> findByEmitenteCnpjAndDataEmissaoBetween(String emitenteCnpj, LocalDateTime start, LocalDateTime end);

    List<CTE> findByStatusOrderByCreatedAtDesc(CTEStatus status);

    @Query("SELECT c FROM CTE c WHERE c.status = :status AND c.createdAt < :cutoff")
    List<CTE> findStuckProcessing(@Param("status") CTEStatus status, @Param("cutoff") LocalDateTime cutoff);

    boolean existsByChaveAcesso(String chaveAcesso);

    Long getMaxNumeroByEmitenteCnpjAndSerie(String emitenteCnpj, String serie);
}