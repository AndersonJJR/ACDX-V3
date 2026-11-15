package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.model.SPEDStatus;
import com.erp.brazil.fiscal.domain.model.SPEDType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SPEDBlockRepository extends JpaRepository<SPEDBlock, String> {
    List<SPEDBlock> findByCompanyCnpjAndReferenceMonthAndSpedType(String companyCnpj, String referenceMonth, SPEDType spedType);

    List<SPEDBlock> findByCompanyCnpjAndSpedTypeOrderByReferenceMonthDesc(String companyCnpj, SPEDType spedType);

    List<SPEDBlock> findByStatusOrderByGeneratedAtDesc(SPEDStatus status);

    @Query("SELECT s FROM SPEDBlock s WHERE s.spedType = :spedType AND s.referenceMonth = :referenceMonth AND s.status = :status")
    List<SPEDBlock> findBySpedTypeAndReferenceMonthAndStatus(@Param("spedType") SPEDType spedType, @Param("referenceMonth") String referenceMonth, @Param("status") SPEDStatus status);

    Optional<SPEDBlock> findByIdAndSpedType(String id, SPEDType spedType);

    Long countByCompanyCnpjAndSpedTypeAndReferenceMonth(String companyCnpj, SPEDType spedType, String referenceMonth);

    void deleteByCompanyCnpjAndSpedTypeAndReferenceMonth(String companyCnpj, SPEDType spedType, String referenceMonth);
}