package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.NCM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NCMRepository extends JpaRepository<NCM, String> {
    Optional<NCM> findByCodeAndActiveTrue(String code);

    List<NCM> findByActiveTrueOrderByCode();

    @Query("SELECT n FROM NCM n WHERE n.code LIKE %:code% OR n.description LIKE %:description%")
    List<NCM> search(@Param("code") String code, @Param("description") String description);

    List<NCM> findByChapter(String chapter);

    boolean existsByCode(String code);
}