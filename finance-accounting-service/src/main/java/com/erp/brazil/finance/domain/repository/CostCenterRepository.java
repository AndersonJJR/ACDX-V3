package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.CostCenter;
import com.erp.brazil.finance.domain.model.CostCenterType;
import com.erp.brazil.finance.domain.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {

    Optional<CostCenter> findByCode(String code);

    List<CostCenter> findByActiveTrueOrderByCode();

    List<CostCenter> findByParentIsNullOrderByCode();

    List<CostCenter> findByParent(CostCenter parent);

    List<CostCenter> findByCostCenterType(CostCenterType costCenterType);

    List<CostCenter> findByManager(Employee manager);

    Page<CostCenter> findByActiveTrue(Pageable pageable);

    Page<CostCenter> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);
}