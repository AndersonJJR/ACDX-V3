package com.erp.brazil.fiscal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sealed interface for SPED (Sistema Público de Escrituração Digital) blocks.
 */
public sealed interface SPEDBlock permits SPEDBlock.ECD, SPEDBlock.ECF, SPEDBlock.EFD_ICMS_IPI, SPEDBlock.EFD_Contribuicoes {

    String getId();
    String getCompanyId();
    String getPeriod();
    LocalDate getStartDate();
    LocalDate getEndDate();
    SPEDStatus getStatus();
    LocalDateTime getGeneratedAt();
    String getFilePath();
    String getFileHash();

    record ECD(
            String id,
            String companyId,
            String period,
            LocalDate startDate,
            LocalDate endDate,
            SPEDStatus status,
            LocalDateTime generatedAt,
            String filePath,
            String fileHash,
            String version,
            Layout layout,
            List<Register> registers
    ) implements SPEDBlock {
        public enum Layout {
            LAYOUT_1, LAYOUT_2, LAYOUT_3
        }

        public record Register(
                String registerCode,
                String parentRegister,
                int lineNumber,
                String content,
                boolean isValid
        ) {}
    }

    record ECF(
            String id,
            String companyId,
            String period,
            LocalDate startDate,
            LocalDate endDate,
            SPEDStatus status,
            LocalDateTime generatedAt,
            String filePath,
            String fileHash,
            String version,
            String scenario,
            List<Register> registers
    ) implements SPEDBlock {
        public record Register(
                String registerCode,
                String parentRegister,
                int lineNumber,
                String content,
                boolean isValid
        ) {}
    }

    record EFD_ICMS_IPI(
            String id,
            String companyId,
            String period,
            LocalDate startDate,
            LocalDate endDate,
            SPEDStatus status,
            LocalDateTime generatedAt,
            String filePath,
            String fileHash,
            String version,
            String profile,
            List<Register> registers
    ) implements SPEDBlock {
        public enum Profile {
            A, B, C
        }

        public record Register(
                String registerCode,
                String parentRegister,
                int lineNumber,
                String content,
                boolean isValid
        ) {}
    }

    record EFD_Contribuicoes(
            String id,
            String companyId,
            String period,
            LocalDate startDate,
            LocalDate endDate,
            SPEDStatus status,
            LocalDateTime generatedAt,
            String filePath,
            String fileHash,
            String version,
            String profile,
            List<Register> registers
    ) implements SPEDBlock {
        public enum Profile {
            A, B, C, D
        }

        public record Register(
                String registerCode,
                String parentRegister,
                int lineNumber,
                String content,
                boolean isValid
        ) {}
    }
}