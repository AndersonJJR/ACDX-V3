package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.NFE;
import com.erp.brazil.fiscal.domain.model.NFEStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NFERepository extends JpaRepository<NFE, String> {
    Optional<NFE> findByChaveAcesso(String chaveAcesso);

    List<NFE> findByEmitenteCnpjAndStatusIn(String emitenteCnpj, List<NFEStatus> statuses);

    List<NFE> findByEmitenteCnpjAndDataEmissaoBetween(String emitenteCnpj, LocalDateTime start, LocalDateTime end);

    List<NFE> findByStatusOrderByCreatedAtDesc(NFEStatus status);

    @Query("SELECT n FROM NFE n WHERE n.status = :status AND n.createdAt < :cutoff")
    List<NFE> findStuckProcessing(@Param("status") NFEStatus status, @Param("cutoff") LocalDateTime cutoff);

    boolean existsByChaveAcesso(String chaveAcesso);

    Long countByEmitenteCnpjAndDataEmissaoBetween(String emitenteCnpj, LocalDateTime start, LocalDateTime end);

    Long getMaxNumeroByEmitenteCnpjAndSerie(String emitenteCnpj, String serie);

    @Query("SELECT n FROM NFE n WHERE n.companyId = :companyId AND n.series = :series AND CAST(n.number AS int) BETWEEN :start AND :end")
    List<NFE> findByCompanyIdAndSeriesAndNumberRange(
            @Param("companyId") String companyId,
            @Param("series") String series,
            @Param("start") int start,
            @Param("end") int end
    );
}