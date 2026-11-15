package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.NFSE;
import com.erp.brazil.fiscal.domain.model.NFSEStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NFSERepository extends JpaRepository<NFSE, String> {
    Optional<NFSE> findByPrestadorCnpjAndNumeroAndSerie(String prestadorCnpj, Long numero, String serie);

    List<NFSE> findByPrestadorCnpjAndStatusIn(String prestadorCnpj, List<NFSEStatus> statuses);

    List<NFSE> findByPrestadorCnpjAndDataEmissaoBetween(String prestadorCnpj, LocalDateTime start, LocalDateTime end);

    List<NFSE> findByStatusOrderByCreatedAtDesc(NFSEStatus status);

    @Query("SELECT n FROM NFSE n WHERE n.status = :status AND n.createdAt < :cutoff")
    List<NFSE> findStuckProcessing(@Param("status") NFSEStatus status, @Param("cutoff") LocalDateTime cutoff);

    Long getMaxNumeroByPrestadorCnpjAndSerie(String prestadorCnpj, String serie);
}