package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.FiscalYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChartOfAccountsRepository extends JpaRepository<ChartOfAccounts, UUID> {

    Optional<ChartOfAccounts> findByCodeAndFiscalYear(String code, FiscalYear fiscalYear);

    Optional<ChartOfAccounts> findByCode(String code);

    List<ChartOfAccounts> findByFiscalYearOrderByCode(FiscalYear fiscalYear);

    List<ChartOfAccounts> findByParentIsNullAndFiscalYearOrderByCode(FiscalYear fiscalYear);

    List<ChartOfAccounts> findByParent(ChartOfAccounts parent);

    List<ChartOfAccounts> findByAccountTypeAndFiscalYearOrderByCode(com.erp.brazil.finance.domain.model.AccountType accountType, FiscalYear fiscalYear);

    List<ChartOfAccounts> findByActiveTrueAndFiscalYearOrderByCode(FiscalYear fiscalYear);

    @Query("SELECT c FROM ChartOfAccounts c WHERE c.fiscalYear = :fiscalYear AND c.code LIKE :pattern% ORDER BY c.code")
    List<ChartOfAccounts> findByCodePrefix(@Param("fiscalYear") FiscalYear fiscalYear, @Param("pattern") String pattern);

    boolean existsByCodeAndFiscalYearAndIdNot(String code, FiscalYear fiscalYear, UUID id);

    Page<ChartOfAccounts> findByFiscalYear(FiscalYear fiscalYear, Pageable pageable);

    Page<ChartOfAccounts> findByActiveTrueAndFiscalYear(FiscalYear fiscalYear, Pageable pageable);
}