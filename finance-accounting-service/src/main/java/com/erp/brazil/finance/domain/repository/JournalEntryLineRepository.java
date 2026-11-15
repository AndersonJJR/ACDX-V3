package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.JournalEntry;
import com.erp.brazil.finance.domain.model.JournalEntryLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, UUID> {

    List<JournalEntryLine> findByJournalEntryOrderByLineNumber(JournalEntry journalEntry);

    List<JournalEntryLine> findByAccount(ChartOfAccounts account);

    Page<JournalEntryLine> findByAccount(ChartOfAccounts account, Pageable pageable);

    List<JournalEntryLine> findByJournalEntry(JournalEntry journalEntry);
}