package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Asset;
import com.erp.brazil.finance.domain.model.DepreciationEntry;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepreciationEntryRepository extends JpaRepository<DepreciationEntry, UUID> {

    List<DepreciationEntry> findByAssetOrderByDepreciationDate(Asset asset);

    List<DepreciationEntry> findByFiscalPeriod(FiscalPeriod fiscalPeriod);

    List<DepreciationEntry> findByAssetAndFiscalPeriod(Asset asset, FiscalPeriod fiscalPeriod);

    Optional<DepreciationEntry> findByJournalEntry(JournalEntry journalEntry);

    Page<DepreciationEntry> findByAsset(Asset asset, Pageable pageable);

    Page<DepreciationEntry> findByFiscalPeriod(FiscalPeriod fiscalPeriod, Pageable pageable);
}