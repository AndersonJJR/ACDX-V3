package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCnpj(String cnpj);

    Optional<Company> findByUuid(String uuid);

    List<Company> findByIsActiveTrue();

    Page<Company> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.cnpj = :cnpj OR c.corporateName LIKE %:term% OR c.tradeName LIKE %:term%")
    List<Company> search(@Param("cnpj") String cnpj, @Param("term") String term);

    boolean existsByCnpj(String cnpj);
}