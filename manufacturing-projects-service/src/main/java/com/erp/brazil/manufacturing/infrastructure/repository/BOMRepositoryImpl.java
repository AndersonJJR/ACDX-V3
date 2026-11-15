package com.erp.brazil.manufacturing.infrastructure.repository;

import com.erp.brazil.manufacturing.domain.model.BOM;
import com.erp.brazil.manufacturing.domain.repository.BOMRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BOMRepositoryImpl implements BOMRepository {

    private final BOMRepository jpaRepository;

    public BOMRepositoryImpl(BOMRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public <S extends BOM> S save(S entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<BOM> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<BOM> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<BOM> findAllById(Iterable<UUID> ids) {
        return jpaRepository.findAllById(ids);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(BOM entity) {
        jpaRepository.delete(entity);
    }

    @Override
    public Optional<BOM> findByProductIdAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UUID productId, BOM.BOMStatus status, java.time.LocalDateTime effectiveFrom, java.time.LocalDateTime effectiveTo) {
        return jpaRepository.findByProductIdAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            productId, status, effectiveFrom, effectiveTo);
    }

    @Override
    public List<BOM> findByProductIdAndStatusOrderByVersionDesc(UUID productId, BOM.BOMStatus status) {
        return jpaRepository.findByProductIdAndStatusOrderByVersionDesc(productId, status);
    }

    @Override
    public List<BOM> findByProductIdAndTypeAndStatus(UUID productId, BOM.BOMType type, BOM.BOMStatus status) {
        return jpaRepository.findByProductIdAndTypeAndStatus(productId, type, status);
    }

    @Override
    public org.springframework.data.domain.Page<BOM> findByStatus(BOM.BOMStatus status, org.springframework.data.domain.Pageable pageable) {
        return jpaRepository.findByStatus(status, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<BOM> findByProductId(UUID productId, org.springframework.data.domain.Pageable pageable) {
        return jpaRepository.findByProductId(productId, pageable);
    }

    @Override
    public List<BOM> findByIsPhantomTrueAndStatus(BOM.BOMStatus status) {
        return jpaRepository.findByIsPhantomTrueAndStatus(status);
    }

    @Override
    public List<BOMRepository.BOMExplosionResult> explodeBOM(UUID bomId) {
        return jpaRepository.explodeBOM(bomId);
    }

    @Override
    public boolean existsByProductIdAndVersion(UUID productId, String version) {
        return jpaRepository.existsByProductIdAndVersion(productId, version);
    }

    @Override
    public long countByProductId(UUID productId) {
        return jpaRepository.countByProductId(productId);
    }
}