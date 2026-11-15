-- V3__partition_sle.sql
-- Partition management for stock_ledger_entries table
-- This script adds monthly partitions for the current and next year

-- Procedure to add a new partition
DELIMITER //

CREATE PROCEDURE add_sle_partition(IN partition_name VARCHAR(64), IN partition_date DATE)
BEGIN
    SET @sql = CONCAT('ALTER TABLE stock_ledger_entries ADD PARTITION (PARTITION ', partition_name, ' VALUES LESS THAN (''zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz'', ''', partition_date, '''));');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //

DELIMITER ;

-- Add partitions for 2025
CALL add_sle_partition('p202501', '2025-02-01');
CALL add_sle_partition('p202502', '2025-03-01');
CALL add_sle_partition('p202503', '2025-04-01');
CALL add_sle_partition('p202504', '2025-05-01');
CALL add_sle_partition('p202505', '2025-06-01');
CALL add_sle_partition('p202506', '2025-07-01');
CALL add_sle_partition('p202507', '2025-08-01');
CALL add_sle_partition('p202508', '2025-09-01');
CALL add_sle_partition('p202509', '2025-10-01');
CALL add_sle_partition('p202510', '2025-11-01');
CALL add_sle_partition('p202511', '2025-12-01');
CALL add_sle_partition('p202512', '2026-01-01');

-- Add partitions for 2026
CALL add_sle_partition('p202601', '2026-02-01');
CALL add_sle_partition('p202602', '2026-03-01');
CALL add_sle_partition('p202603', '2026-04-01');
CALL add_sle_partition('p202604', '2026-05-01');
CALL add_sle_partition('p202605', '2026-06-01');
CALL add_sle_partition('p202606', '2026-07-01');
CALL add_sle_partition('p202607', '2026-08-01');
CALL add_sle_partition('p202608', '2026-09-01');
CALL add_sle_partition('p202609', '2026-10-01');
CALL add_sle_partition('p202610', '2026-11-01');
CALL add_sle_partition('p202611', '2026-12-01');
CALL add_sle_partition('p202612', '2027-01-01');

-- Procedure to drop old partitions (for data retention)
DELIMITER //

CREATE PROCEDURE drop_sle_partition(IN partition_name VARCHAR(64))
BEGIN
    SET @sql = CONCAT('ALTER TABLE stock_ledger_entries DROP PARTITION ', partition_name, ';');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //

DELIMITER ;

-- Event to automatically add new partitions monthly
-- This event runs on the 1st of each month at 01:00
DELIMITER //

CREATE EVENT IF NOT EXISTS add_monthly_sle_partition
ON SCHEDULE EVERY 1 MONTH
STARTS TIMESTAMP(DATE_ADD(DATE_ADD(LAST_DAY(CURDATE()), INTERVAL 1 DAY), INTERVAL 1 HOUR))
ON COMPLETION PRESERVE
ENABLE
DO
BEGIN
    DECLARE next_month DATE;
    DECLARE partition_name VARCHAR(64);
    
    SET next_month = DATE_ADD(LAST_DAY(CURDATE()), INTERVAL 1 DAY);
    SET partition_name = CONCAT('p', DATE_FORMAT(next_month, '%Y%m'));
    
    CALL add_sle_partition(partition_name, DATE_ADD(next_month, INTERVAL 1 MONTH));
END //

DELIMITER ;

-- Event to drop partitions older than 7 years (data retention policy)
-- This event runs on the 1st of each month at 02:00
DELIMITER //

CREATE EVENT IF NOT EXISTS drop_old_sle_partitions
ON SCHEDULE EVERY 1 MONTH
STARTS TIMESTAMP(DATE_ADD(DATE_ADD(LAST_DAY(CURDATE()), INTERVAL 1 DAY), INTERVAL 2 HOUR))
ON COMPLETION PRESERVE
ENABLE
DO
BEGIN
    DECLARE cutoff_date DATE;
    DECLARE done INT DEFAULT FALSE;
    DECLARE partition_name VARCHAR(64);
    DECLARE partition_description VARCHAR(1024);
    
    DECLARE cur CURSOR FOR
        SELECT PARTITION_NAME, PARTITION_DESCRIPTION
        FROM INFORMATION_SCHEMA.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'stock_ledger_entries'
        AND PARTITION_NAME != 'pmax'
        AND PARTITION_DESCRIPTION < CONCAT('''zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz'', ''', DATE_SUB(CURDATE(), INTERVAL 7 YEAR), '''');
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO partition_name, partition_description;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        CALL drop_sle_partition(partition_name);
    END LOOP;
    
    CLOSE cur;
END //

DELIMITER ;

-- Reorganize pmax partition to create new partitions up to current date
-- This is a maintenance procedure to be run manually if needed
DELIMITER //

CREATE PROCEDURE reorganize_sle_partitions()
BEGIN
    DECLARE current_date DATE DEFAULT CURDATE();
    DECLARE partition_date DATE;
    DECLARE partition_name VARCHAR(64);
    DECLARE year INT;
    DECLARE month INT;
    
    -- Start from 2024-01
    SET year = 2024;
    SET month = 1;
    
    WHILE MAKEDATE(year, 1) + INTERVAL month MONTH <= DATE_ADD(current_date, INTERVAL 1 MONTH) DO
        SET partition_name = CONCAT('p', year, LPAD(month, 2, '0'));
        SET partition_date = DATE_ADD(MAKEDATE(year, 1), INTERVAL month MONTH);
        
        -- Check if partition already exists
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.PARTITIONS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'stock_ledger_entries' 
            AND PARTITION_NAME = partition_name
        ) THEN
            CALL add_sle_partition(partition_name, partition_date);
        END IF;
        
        SET month = month + 1;
        IF month > 12 THEN
            SET month = 1;
            SET year = year + 1;
        END IF;
    END WHILE;
END //

DELIMITER ;

-- Call reorganization to ensure all partitions up to current date exist
CALL reorganize_sle_partitions();