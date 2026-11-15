-- V3__partition_sle.sql
-- Partition stock_ledger_entries table by company_id + posting_date (monthly partitions)

-- Note: MySQL 8.0+ supports native partitioning
-- This migration creates a partitioned version of stock_ledger_entries
-- and migrates data from the non-partitioned table

-- Step 1: Create new partitioned table
CREATE TABLE `stock_ledger_entries_partitioned` (
    `id` CHAR(36) NOT NULL,
    `company_id` CHAR(36) NOT NULL,
    `item_id` CHAR(36) NOT NULL,
    `item_variant_id` CHAR(36),
    `warehouse_id` CHAR(36) NOT NULL,
    `location_id` CHAR(36),
    `batch_id` CHAR(36),
    `serial_number_id` CHAR(36),
    `qty_change` DECIMAL(20,6) NOT NULL,
    `valuation_rate` DECIMAL(20,6) NOT NULL,
    `stock_value` DECIMAL(20,6) NOT NULL,
    `stock_qty_after` DECIMAL(20,6) NOT NULL,
    `stock_value_after` DECIMAL(20,6) NOT NULL,
    `voucher_type` VARCHAR(50) NOT NULL,
    `voucher_no` VARCHAR(100) NOT NULL,
    `voucher_detail_no` VARCHAR(100),
    `posting_date` TIMESTAMP NOT NULL,
    `posting_time` TIMESTAMP NOT NULL,
    `actual_qty` DECIMAL(20,6),
    `incoming_rate` DECIMAL(20,6),
    `outgoing_rate` DECIMAL(20,6),
    `valuation_method` VARCHAR(20),
    `is_cancelled` BOOLEAN DEFAULT FALSE,
    `is_reposted` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `version` BIGINT DEFAULT 0,
    `partition_key` VARCHAR(50) GENERATED ALWAYS AS (CONCAT(`company_id`, '_', DATE_FORMAT(`posting_date`, '%Y%m'))) STORED,
    PRIMARY KEY (`id`, `partition_key`),
    INDEX `idx_sle_item_warehouse_date` (`item_id`, `warehouse_id`, `posting_date`),
    INDEX `idx_sle_voucher` (`voucher_type`, `voucher_no`),
    INDEX `idx_sle_company_date` (`company_id`, `posting_date`),
    INDEX `idx_sle_batch` (`batch_id`),
    INDEX `idx_sle_serial` (`serial_number_id`),
    INDEX `idx_sle_partition` (`partition_key`),
    INDEX `idx_sle_cancelled` (`is_cancelled`),
    INDEX `idx_sle_reposted` (`is_reposted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY LIST COLUMNS (`partition_key`) (
    PARTITION `p_202401` VALUES IN ('00000000-0000-0000-0000-000000000001_202401'),
    PARTITION `p_202402` VALUES IN ('00000000-0000-0000-0000-000000000001_202402'),
    PARTITION `p_202403` VALUES IN ('00000000-0000-0000-0000-000000000001_202403'),
    PARTITION `p_202404` VALUES IN ('00000000-0000-0000-0000-000000000001_202404'),
    PARTITION `p_202405` VALUES IN ('00000000-0000-0000-0000-000000000001_202405'),
    PARTITION `p_202406` VALUES IN ('00000000-0000-0000-0000-000000000001_202406'),
    PARTITION `p_202407` VALUES IN ('00000000-0000-0000-0000-000000000001_202407'),
    PARTITION `p_202408` VALUES IN ('00000000-0000-0000-0000-000000000001_202408'),
    PARTITION `p_202409` VALUES IN ('00000000-0000-0000-0000-000000000001_202409'),
    PARTITION `p_202410` VALUES IN ('00000000-0000-0000-0000-000000000001_202410'),
    PARTITION `p_202411` VALUES IN ('00000000-0000-0000-0000-000000000001_202411'),
    PARTITION `p_202412` VALUES IN ('00000000-0000-0000-0000-000000000001_202412'),
    PARTITION `p_202501` VALUES IN ('00000000-0000-0000-0000-000000000001_202501'),
    PARTITION `p_202502` VALUES IN ('00000000-0000-0000-0000-000000000001_202502'),
    PARTITION `p_202503` VALUES IN ('00000000-0000-0000-0000-000000000001_202503'),
    PARTITION `p_202504` VALUES IN ('00000000-0000-0000-0000-000000000001_202504'),
    PARTITION `p_202505` VALUES IN ('00000000-0000-0000-0000-000000000001_202505'),
    PARTITION `p_202506` VALUES IN ('00000000-0000-0000-0000-000000000001_202506'),
    PARTITION `p_202507` VALUES IN ('00000000-0000-0000-0000-000000000001_202507'),
    PARTITION `p_202508` VALUES IN ('00000000-0000-0000-0000-000000000001_202508'),
    PARTITION `p_202509` VALUES IN ('00000000-0000-0000-0000-000000000001_202509'),
    PARTITION `p_202510` VALUES IN ('00000000-0000-0000-0000-000000000001_202510'),
    PARTITION `p_202511` VALUES IN ('00000000-0000-0000-0000-000000000001_202511'),
    PARTITION `p_202512` VALUES IN ('00000000-0000-0000-0000-000000000001_202512'),
    PARTITION `p_202601` VALUES IN ('00000000-0000-0000-0000-000000000001_202601'),
    PARTITION `p_202602` VALUES IN ('00000000-0000-0000-0000-000000000001_202602'),
    PARTITION `p_202603` VALUES IN ('00000000-0000-0000-0000-000000000001_202603'),
    PARTITION `p_202604` VALUES IN ('00000000-0000-0000-0000-000000000001_202604'),
    PARTITION `p_202605` VALUES IN ('00000000-0000-0000-0000-000000000001_202605'),
    PARTITION `p_202606` VALUES IN ('00000000-0000-0000-0000-000000000001_202606'),
    PARTITION `p_202607` VALUES IN ('00000000-0000-0000-0000-000000000001_202607'),
    PARTITION `p_202608` VALUES IN ('00000000-0000-0000-0000-000000000001_202608'),
    PARTITION `p_202609` VALUES IN ('00000000-0000-0000-0000-000000000001_202609'),
    PARTITION `p_202610` VALUES IN ('00000000-0000-0000-0000-000000000001_202610'),
    PARTITION `p_202611` VALUES IN ('00000000-0000-0000-0000-000000000001_202611'),
    PARTITION `p_202612` VALUES IN ('00000000-0000-0000-0000-000000000001_202612'),
    PARTITION `p_future` VALUES IN (DEFAULT)
);

-- Step 2: Migrate data (run this in a separate transaction or with low priority)
-- INSERT INTO `stock_ledger_entries_partitioned` SELECT * FROM `stock_ledger_entries`;

-- Step 3: Swap tables (run during maintenance window)
-- RENAME TABLE `stock_ledger_entries` TO `stock_ledger_entries_old`,
--              `stock_ledger_entries_partitioned` TO `stock_ledger_entries`;

-- Step 4: Recreate foreign keys on partitioned table (MySQL 8.0+ supports FKs on partitioned tables)
-- ALTER TABLE `stock_ledger_entries` 
--     ADD CONSTRAINT `fk_sle_item` FOREIGN KEY (`item_id`) REFERENCES `items`(`id`),
--     ADD CONSTRAINT `fk_sle_item_variant` FOREIGN KEY (`item_variant_id`) REFERENCES `item_variants`(`id`),
--     ADD CONSTRAINT `fk_sle_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouses`(`id`),
--     ADD CONSTRAINT `fk_sle_batch` FOREIGN KEY (`batch_id`) REFERENCES `item_batches`(`id`),
--     ADD CONSTRAINT `fk_sle_serial` FOREIGN KEY (`serial_number_id`) REFERENCES `item_serial_numbers`(`id`);

-- Alternative: Range partitioning by posting_date (simpler for multi-company)
-- Uncomment below if you prefer range partitioning over list partitioning

/*
CREATE TABLE `stock_ledger_entries_range` (
    `id` CHAR(36) NOT NULL,
    `company_id` CHAR(36) NOT NULL,
    `item_id` CHAR(36) NOT NULL,
    `item_variant_id` CHAR(36),
    `warehouse_id` CHAR(36) NOT NULL,
    `location_id` CHAR(36),
    `batch_id` CHAR(36),
    `serial_number_id` CHAR(36),
    `qty_change` DECIMAL(20,6) NOT NULL,
    `valuation_rate` DECIMAL(20,6) NOT NULL,
    `stock_value` DECIMAL(20,6) NOT NULL,
    `stock_qty_after` DECIMAL(20,6) NOT NULL,
    `stock_value_after` DECIMAL(20,6) NOT NULL,
    `voucher_type` VARCHAR(50) NOT NULL,
    `voucher_no` VARCHAR(100) NOT NULL,
    `voucher_detail_no` VARCHAR(100),
    `posting_date` TIMESTAMP NOT NULL,
    `posting_time` TIMESTAMP NOT NULL,
    `actual_qty` DECIMAL(20,6),
    `incoming_rate` DECIMAL(20,6),
    `outgoing_rate` DECIMAL(20,6),
    `valuation_method` VARCHAR(20),
    `is_cancelled` BOOLEAN DEFAULT FALSE,
    `is_reposted` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `version` BIGINT DEFAULT 0,
    PRIMARY KEY (`id`, `posting_date`),
    INDEX `idx_sle_item_warehouse_date` (`item_id`, `warehouse_id`, `posting_date`),
    INDEX `idx_sle_voucher` (`voucher_type`, `voucher_no`),
    INDEX `idx_sle_company_date` (`company_id`, `posting_date`),
    INDEX `idx_sle_batch` (`batch_id`),
    INDEX `idx_sle_serial` (`serial_number_id`),
    INDEX `idx_sle_cancelled` (`is_cancelled`),
    INDEX `idx_sle_reposted` (`is_reposted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (YEAR(`posting_date`) * 100 + MONTH(`posting_date`)) (
    PARTITION `p_202401` VALUES LESS THAN (202402),
    PARTITION `p_202402` VALUES LESS THAN (202403),
    PARTITION `p_202403` VALUES LESS THAN (202404),
    PARTITION `p_202404` VALUES LESS THAN (202405),
    PARTITION `p_202405` VALUES LESS THAN (202406),
    PARTITION `p_202406` VALUES LESS THAN (202407),
    PARTITION `p_202407` VALUES LESS THAN (202408),
    PARTITION `p_202408` VALUES LESS THAN (202409),
    PARTITION `p_202409` VALUES LESS THAN (202410),
    PARTITION `p_202410` VALUES LESS THAN (202411),
    PARTITION `p_202411` VALUES LESS THAN (202412),
    PARTITION `p_202412` VALUES LESS THAN (202501),
    PARTITION `p_202501` VALUES LESS THAN (202502),
    PARTITION `p_202502` VALUES LESS THAN (202503),
    PARTITION `p_202503` VALUES LESS THAN (202504),
    PARTITION `p_202504` VALUES LESS THAN (202505),
    PARTITION `p_202505` VALUES LESS THAN (202506),
    PARTITION `p_202506` VALUES LESS THAN (202507),
    PARTITION `p_202507` VALUES LESS THAN (202508),
    PARTITION `p_202508` VALUES LESS THAN (202509),
    PARTITION `p_202509` VALUES LESS THAN (202510),
    PARTITION `p_202510` VALUES LESS THAN (202511),
    PARTITION `p_202511` VALUES LESS THAN (202512),
    PARTITION `p_202512` VALUES LESS THAN (202601),
    PARTITION `p_202601` VALUES LESS THAN (202602),
    PARTITION `p_202602` VALUES LESS THAN (202603),
    PARTITION `p_202603` VALUES LESS THAN (202604),
    PARTITION `p_202604` VALUES LESS THAN (202605),
    PARTITION `p_202605` VALUES LESS THAN (202606),
    PARTITION `p_202606` VALUES LESS THAN (202607),
    PARTITION `p_202607` VALUES LESS THAN (202608),
    PARTITION `p_202608` VALUES LESS THAN (202609),
    PARTITION `p_202609` VALUES LESS THAN (202610),
    PARTITION `p_202610` VALUES LESS THAN (202611),
    PARTITION `p_202611` VALUES LESS THAN (202612),
    PARTITION `p_202612` VALUES LESS THAN (202701),
    PARTITION `p_future` VALUES LESS THAN MAXVALUE
);
*/

-- Procedure to add new partitions automatically (run monthly via event scheduler)
DELIMITER //

CREATE PROCEDURE `add_sle_partition`(IN p_company_id CHAR(36), IN p_year_month INT)
BEGIN
    DECLARE v_partition_name VARCHAR(64);
    DECLARE v_value VARCHAR(50);
    DECLARE v_sql TEXT;
    
    SET v_partition_name = CONCAT('p_', p_year_month);
    SET v_value = CONCAT(p_company_id, '_', p_year_month);
    
    SET v_sql = CONCAT('ALTER TABLE `stock_ledger_entries_partitioned` ADD PARTITION (PARTITION `', v_partition_name, '` VALUES IN (\'', v_value, '\'))');
    
    PREPARE stmt FROM v_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //

CREATE PROCEDURE `add_sle_range_partition`(IN p_year_month INT)
BEGIN
    DECLARE v_partition_name VARCHAR(64);
    DECLARE v_next_month INT;
    DECLARE v_sql TEXT;
    
    SET v_partition_name = CONCAT('p_', p_year_month);
    SET v_next_month = p_year_month + 1;
    IF v_next_month % 100 = 13 THEN
        SET v_next_month = v_next_month + 88; -- Handle year rollover (e.g., 202413 -> 202501)
    END IF;
    
    SET v_sql = CONCAT('ALTER TABLE `stock_ledger_entries_range` ADD PARTITION (PARTITION `', v_partition_name, '` VALUES LESS THAN (', v_next_month, '))');
    
    PREPARE stmt FROM v_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //

DELIMITER ;

-- Event to automatically add partitions (enable event_scheduler in MySQL)
-- CREATE EVENT `add_monthly_sle_partition`
-- ON SCHEDULE EVERY 1 MONTH
-- STARTS CURRENT_TIMESTAMP + INTERVAL 1 MONTH
-- DO CALL add_sle_range_partition(YEAR(DATE_ADD(NOW(), INTERVAL 1 MONTH)) * 100 + MONTH(DATE_ADD(NOW(), INTERVAL 1 MONTH)));