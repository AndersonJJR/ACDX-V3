package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Union;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnionRepository extends JpaRepository<Union, Long> {

    Optional<Union> findByCode(String code);

    List<Union> findByIsActiveTrue();
}