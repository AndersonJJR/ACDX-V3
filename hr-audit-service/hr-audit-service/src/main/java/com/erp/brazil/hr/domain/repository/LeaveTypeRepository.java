package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.LeaveType;
import com.erp.brazil.hr.domain.model.LeaveCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByCompanyIdAndCode(Long companyId, String code);

    List<LeaveType> findByCompanyId(Long companyId);

    Page<LeaveType> findByCompanyId(Long companyId, Pageable pageable);

    List<LeaveType> findByCompanyIdAndIsActiveTrue(Long companyId);

    List<LeaveType> findByCompanyIdAndCategory(Long companyId, LeaveCategory category);

    List<LeaveType> findByCompanyIdAndCategoryAndIsActiveTrue(Long companyId, LeaveCategory category);

    @Query("SELECT lt FROM LeaveType lt WHERE lt.companyId = :companyId AND lt.isActive = true AND lt.category IN :categories")
    List<LeaveType> findByCompanyIdAndCategories(@Param("companyId") Long companyId,
                                                  @Param("categories") List<LeaveCategory> categories);

    boolean existsByCompanyIdAndCode(Long companyId, String code);
}