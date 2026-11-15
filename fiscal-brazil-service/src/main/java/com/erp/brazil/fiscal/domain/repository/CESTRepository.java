package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.CEST;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CESTRepository extends JpaRepository<CEST, String> {
    Optional<CEST> findByCodeAndActiveTrue(String code);

    List<CEST> findByActiveTrueOrderByCode();

    @Query("SELECT c FROM CEST c WHERE c.code LIKE %:code% OR c.description LIKE %:description%")
    List<CEST> search(@Param("code") String code, @Param("description") String description);

    List<CEST> findByNcmCode(String ncmCode);

    List<CEST> findBySegment(String segment);

    boolean existsByCode(String code);
}