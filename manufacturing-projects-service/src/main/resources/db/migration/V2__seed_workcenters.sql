-- V2__seed_workcenters.sql
-- Seed initial work centers for manufacturing

INSERT INTO work_center (id, code, name, type, status, description, capacity_per_day_minutes, efficiency_percentage, utilization_percentage, hourly_rate, currency, created_at, created_by) VALUES
(UUID(), 'WC-MACH-01', 'CNC Machining Center 1', 'MACHINE', 'ACTIVE', '5-axis CNC machining center', 480, 95.00, 85.00, 150.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-MACH-02', 'CNC Machining Center 2', 'MACHINE', 'ACTIVE', '3-axis CNC machining center', 480, 90.00, 80.00, 120.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-MACH-03', 'CNC Turning Center', 'MACHINE', 'ACTIVE', 'CNC lathe with live tooling', 480, 92.00, 82.00, 130.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-ASM-01', 'Assembly Station 1', 'ASSEMBLY', 'ACTIVE', 'Main assembly line station 1', 480, 100.00, 90.00, 80.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-ASM-02', 'Assembly Station 2', 'ASSEMBLY', 'ACTIVE', 'Main assembly line station 2', 480, 100.00, 90.00, 80.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-ASM-03', 'Sub-Assembly Station', 'ASSEMBLY', 'ACTIVE', 'Sub-assembly preparation station', 480, 95.00, 85.00, 75.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-INS-01', 'Incoming Inspection', 'INSPECTION', 'ACTIVE', 'Incoming material inspection station', 480, 100.00, 75.00, 90.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-INS-02', 'In-Process Inspection', 'INSPECTION', 'ACTIVE', 'In-process quality inspection', 480, 100.00, 70.00, 90.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-INS-03', 'Final Inspection', 'INSPECTION', 'ACTIVE', 'Final product inspection', 480, 100.00, 80.00, 95.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-PKG-01', 'Packaging Station', 'PACKAGING', 'ACTIVE', 'Product packaging and labeling', 480, 95.00, 85.00, 70.0000, 'BRL', NOW(), 'system'),
(UUUID(), 'WC-WH-01', 'Raw Material Warehouse', 'WAREHOUSE', 'ACTIVE', 'Raw material storage and picking', 480, 90.00, 80.00, 60.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-WH-02', 'Finished Goods Warehouse', 'WAREHOUSE', 'ACTIVE', 'Finished goods storage and shipping', 480, 90.00, 80.00, 60.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-SUB-01', 'Subcontracting Vendor A', 'SUBCONTRACTING', 'ACTIVE', 'External heat treatment vendor', 480, 85.00, 70.00, 200.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-SUB-02', 'Subcontracting Vendor B', 'SUBCONTRACTING', 'ACTIVE', 'External coating vendor', 480, 85.00, 70.00, 180.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-LBR-01', 'Manual Labor Pool', 'LABOR', 'ACTIVE', 'General manual labor pool', 480, 95.00, 90.00, 50.0000, 'BRL', NOW(), 'system'),
(UUID(), 'WC-LBR-02', 'Skilled Labor Pool', 'LABOR', 'ACTIVE', 'Skilled manual labor (welding, etc.)', 480, 90.00, 85.00, 85.0000, 'BRL', NOW(), 'system');

-- Seed work stations for each work center
-- WC-MACH-01 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-MACH-01-01', 'CNC Machine 1 - Station 1', 'ACTIVE', 480, 95.00, 150.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-MACH-01';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-MACH-01-02', 'CNC Machine 1 - Station 2', 'ACTIVE', 480, 95.00, 150.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-MACH-01';

-- WC-MACH-02 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-MACH-02-01', 'CNC Machine 2 - Station 1', 'ACTIVE', 480, 90.00, 120.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-MACH-02';

-- WC-MACH-03 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-MACH-03-01', 'CNC Turning - Station 1', 'ACTIVE', 480, 92.00, 130.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-MACH-03';

-- WC-ASM-01 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-ASM-01-01', 'Assembly Line 1 - Position 1', 'ACTIVE', 480, 100.00, 80.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-ASM-01';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-ASM-01-02', 'Assembly Line 1 - Position 2', 'ACTIVE', 480, 100.00, 80.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-ASM-01';

-- WC-ASM-02 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-ASM-02-01', 'Assembly Line 2 - Position 1', 'ACTIVE', 480, 100.00, 80.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-ASM-02';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-ASM-02-02', 'Assembly Line 2 - Position 2', 'ACTIVE', 480, 100.00, 80.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-ASM-02';

-- WC-ASM-03 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-ASM-03-01', 'Sub-Assembly Station 1', 'ACTIVE', 480, 95.00, 75.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-ASM-03';

-- WC-INS-01 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-INS-01-01', 'Incoming Inspection Station 1', 'ACTIVE', 480, 100.00, 90.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-INS-01';

-- WC-INS-02 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-INS-02-01', 'In-Process Inspection Station 1', 'ACTIVE', 480, 100.00, 90.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-INS-02';

-- WC-INS-03 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-INS-03-01', 'Final Inspection Station 1', 'ACTIVE', 480, 100.00, 95.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-INS-03';

-- WC-PKG-01 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-PKG-01-01', 'Packaging Station 1', 'ACTIVE', 480, 95.00, 70.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-PKG-01';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-PKG-01-02', 'Packaging Station 2', 'ACTIVE', 480, 95.00, 70.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-PKG-01';

-- WC-WH-01 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-WH-01-01', 'Raw Material Picking Station 1', 'ACTIVE', 480, 90.00, 60.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-WH-01';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-WH-01-02', 'Raw Material Picking Station 2', 'ACTIVE', 480, 90.00, 60.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-WH-01';

-- WC-WH-02 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-WH-02-01', 'Finished Goods Shipping Station 1', 'ACTIVE', 480, 90.00, 60.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-WH-02';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-WH-02-02', 'Finished Goods Shipping Station 2', 'ACTIVE', 480, 90.00, 60.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-WH-02';

-- WC-SUB-01 stations (virtual stations for subcontracting)
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-SUB-01-01', 'Vendor A - Heat Treatment', 'ACTIVE', 480, 85.00, 200.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-SUB-01';

-- WC-SUB-02 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-SUB-02-01', 'Vendor B - Coating', 'ACTIVE', 480, 85.00, 180.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-SUB-02';

-- WC-LBR-01 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-LBR-01-01', 'General Labor Station 1', 'ACTIVE', 480, 95.00, 50.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-LBR-01';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-LBR-01-02', 'General Labor Station 2', 'ACTIVE', 480, 95.00, 50.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-LBR-01';

-- WC-LBR-02 stations
INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-LBR-02-01', 'Welding Station 1', 'ACTIVE', 480, 90.00, 85.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-LBR-02';

INSERT INTO work_station (id, work_center_id, code, name, status, capacity_per_day_minutes, efficiency_percentage, hourly_rate, currency, created_at, created_by)
SELECT UUID(), wc.id, 'WS-LBR-02-02', 'Welding Station 2', 'ACTIVE', 480, 90.00, 85.0000, 'BRL', NOW(), 'system'
FROM work_center wc WHERE wc.code = 'WC-LBR-02';