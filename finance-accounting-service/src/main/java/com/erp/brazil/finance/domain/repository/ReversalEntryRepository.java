package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.JournalEntry;
import com.erp.brazil.finance.domain.model.ReversalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReversalEntryRepository extends JpaRepository<ReversalEntry, UUID> {

    List<ReversalEntry> findByOriginalEntry(JournalEntry originalEntry);

    List<ReversalEntry> findByReversalEntry(JournalEntry reversalEntry);

    Optional<ReversalEntry> findByOriginalEntry(JournalEntry originalEntry);

    Page<ReversalEntry> findAll(Pageable pageable);
}