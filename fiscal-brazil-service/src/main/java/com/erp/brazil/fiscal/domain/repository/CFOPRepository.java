package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.CFOP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CFOPRepository extends JpaRepository<CFOP, String> {
    Optional<CFOP> findByCodeAndActiveTrue(String code);

    List<CFOP> findByActiveTrueOrderByCode();

    @Query("SELECT c FROM CFOP c WHERE c.code LIKE %:code% OR c.description LIKE %:description%")
    List<CFOP> search(@Param("code") String code, @Param("description") String description);

    List<CFOP> findByType(String type);

    List<CFOP> findByCategory(String category);

    List<CFOP> findByGeneratesIcmsTrue();

    boolean existsByCode(String code);
}