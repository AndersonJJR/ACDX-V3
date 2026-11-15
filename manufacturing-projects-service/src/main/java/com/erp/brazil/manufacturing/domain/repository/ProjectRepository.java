package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.Project;
import com.erp.brazil.manufacturing.domain.model.Project.ProjectStatus;
import com.erp.brazil.manufacturing.domain.model.Project.ProjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByCode(String code);

    List<Project> findByCustomerId(UUID customerId);

    List<Project> findByProjectManagerId(UUID projectManagerId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByProjectType(ProjectType projectType);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    Page<Project> findByProjectManagerId(UUID projectManagerId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.startDate <= :date AND p.endDate >= :date AND p.status = 'ACTIVE'")
    List<Project> findActiveProjectsAtDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM Project p WHERE p.endDate < :date AND p.status IN ('PLANNING', 'ACTIVE', 'ON_HOLD')")
    List<Project> findOverdueProjects(@Param("date") LocalDate date);

    boolean existsByCode(String code);
}