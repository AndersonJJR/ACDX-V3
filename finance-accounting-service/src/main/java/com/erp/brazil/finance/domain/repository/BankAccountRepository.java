package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.BankAccount;
import com.erp.brazil.finance.domain.model.BankAccountType;
import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    Optional<BankAccount> findByCode(String code);

    Optional<BankAccount> findByBankCodeAndBranchCodeAndAccountNumber(String bankCode, String branchCode, String accountNumber);

    List<BankAccount> findByActiveTrueOrderByCode();

    List<BankAccount> findByAccountType(BankAccountType accountType);

    List<BankAccount> findByCurrency(Currency currency);

    List<BankAccount> findByGlAccount(ChartOfAccounts glAccount);

    Page<BankAccount> findByActiveTrue(Pageable pageable);

    Page<BankAccount> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByBankCodeAndBranchCodeAndAccountNumberAndIdNot(String bankCode, String branchCode, String accountNumber, UUID id);
}