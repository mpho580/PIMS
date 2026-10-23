-- ============================================================
-- HealthFirst PIMS - Complete database script
-- Based on current pims_db data (users, suppliers, medicines,
-- sales, sale_items)
-- ============================================================

CREATE DATABASE IF NOT EXISTS pims_db;
USE pims_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS medicines;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- --------------------------------------------------------
-- users
-- Passwords stored as SHA-256 (matches your working app)
-- admin123  -> SHA2('admin123', 256)
-- cash123   -> SHA2('cash123', 256)
-- --------------------------------------------------------
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    role        ENUM('Admin', 'Cashier') NOT NULL DEFAULT 'Cashier',
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (user_id, username, password, full_name, role, is_active, created_at) VALUES
(1, 'admin',   SHA2('admin123', 256), 'System Administrator', 'Admin',   1, '2026-10-22 01:01:23'),
(2, 'cashier', SHA2('cash123', 256),  'Mpho Cashier',         'Cashier', 1, '2026-10-22 01:01:23'),
(3, 'Tebogo',  SHA2('admin123', 256), 'Tebogo Manager',       'Admin',   1, '2026-10-22 18:27:26');

-- --------------------------------------------------------
-- suppliers
-- --------------------------------------------------------
CREATE TABLE suppliers (
    supplier_id    INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name  VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    phone          VARCHAR(20),
    email          VARCHAR(100),
    address        VARCHAR(255),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO suppliers (supplier_id, supplier_name, contact_person, phone, email, address, created_at) VALUES
(1,  'PharmaCorp Ltd',            'David Chen',      '011-555-1001', 'orders@pharmacorp.com',           '12 Industrial Road, Johannesburg', '2026-10-22 01:06:33'),
(2,  'MediSupply SA',             'Thandi Nkosi',    '021-555-2002', 'sales@medisupply.co.za',           '45 Health Ave, Cape Town',         '2026-10-22 01:06:33'),
(3,  'Global Meds Inc',           'James Wilson',    '031-555-3003', 'info@globalmeds.com',              '88 Pharma Park, Durban',           '2026-10-22 01:06:33'),
(4,  'Local Distributors',        'Anita Patel',     '012-555-4004', 'anita@localdist.co.za',            '3 Warehouse Lane, Pretoria',       '2026-10-22 01:06:33'),
(5,  'MedPlus',                   'Sarah Mokoena',   '011-555-2001', 'sarah@medplus.example.com',        '10 Medical Park, Johannesburg',    '2026-10-23 01:55:41'),
(6,  'Cape Health Supplies',      'Peter Jacobs',    '021-555-2002', 'Peter@capehealth.example.com',     '25 Health Street, Cape Town',      '2026-10-23 01:56:46'),
(7,  'Gauteng Pharma Wholesale',  'Lerato Molefe',   '012-555-2003', 'lerato@gautengpharma.example.com', '18 Warehouse Road, Pretoria',      '2026-10-23 01:57:56'),
(8,  'Wellness Medical Supplies', 'Thabo Nkosi',     '031-555-2004', 'thabo@wellness.example.com',       '42 Pharmacy Lane, Durban',         '2026-10-23 01:59:09'),
(9,  'VitalCare Pharmaceuticals', 'Nomsa Dlamini',   '011-555-2005', 'nomsa@vitalcare.example.com',      '7 Industrial Avenue, Johannesburg','2026-10-23 02:00:09'),
(10, 'MediLink Distributors',     'James Smith',     '021-555-2006', 'James@medilink.example.com',       '14 Harbour Road, Cape Town',       '2026-10-23 02:01:25'),
(11, 'PrimeMed Wholesale',        'Zanele Khumalo',  '012-555-2007', 'zanele@primemed.example.com',      '30 Logistics Park, Centurion',     '2026-10-23 02:05:08'),
(12, 'LifeSource Medical',        'David Naidoo',    '031-555-2008', 'david@lifesource.example.com',     '9 Health Avenue, Durban',          '2026-10-23 02:05:51'),
(13, 'PharmaCare Distributors',   'Amanda Botha',    '011-555-2009', 'amanda@pharmacare.example.com',    '55 Industrial Road, Sandton',      '2026-10-23 02:06:50'),
(14, 'Essential Medicines SA',    'Kabelo Sithole',  '012-555-2010', 'kabelo@essentialmeds.example.com', '22 Supply Chain Street, Pretoria', '2026-10-23 02:07:57');

-- --------------------------------------------------------
-- medicines
-- --------------------------------------------------------
CREATE TABLE medicines (
    medicine_id       INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(150) NOT NULL,
    company           VARCHAR(100),
    medicine_type     VARCHAR(50),
    price             DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level     INT NOT NULL DEFAULT 10,
    expiry_date       DATE,
    supplier_id       INT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicine_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

INSERT INTO medicines (medicine_id, name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id, created_at) VALUES
(1,  'Paracetamol 500mg',    'Aspen',        'Tablet',    45.50,  250, 50, '2027-06-15', 1, '2026-10-22 01:10:40'),
(2,  'Amoxicillin 250mg',    'Pfizer',       'Capsule',   89.99,   80, 30, '2026-12-01', 1, '2026-10-22 01:10:40'),
(3,  'Cough Syrup 100ml',    'Adcock',       'Syrup',     65.00,  117, 40, '2026-09-30', 2, '2026-10-22 01:10:40'),
(4,  'Insulin Injection',    'Novo Nordisk', 'Injection', 320.00,   25, 10, '2026-11-20', 3, '2026-10-22 01:10:40'),
(5,  'Hydrocortisone Cream', 'Cipla',        'Cream',     55.75,   60, 20, '2027-03-10', 2, '2026-10-22 01:10:40'),
(6,  'Ibuprofen 400mg',      'Aspen',        'Tablet',    52.00,  180, 40, '2027-01-25', 1, '2026-10-22 01:10:40'),
(7,  'Vitamin C 1000mg',     'Dischem',      'Tablet',   120.00,   14, 25, '2026-10-05', 4, '2026-10-22 01:10:40'),
(8,  'Omeprazole 20mg',      'AstraZeneca',  'Capsule',  145.50,   95, 30, '2027-08-18', 3, '2026-10-22 01:10:40'),
(9,  'Salbutamol Inhaler',   'GSK',          'Injection',210.00,   40, 15, '2026-10-15', 2, '2026-10-22 01:10:40'),
(10, 'Metformin 500mg',      'Cipla',        'Tablet',    78.25,  197, 50, '2027-05-30', 4, '2026-10-22 01:10:40'),
(11, 'Cetirizine 10mg',      'Aspen',        'Tablet',    38.00,    8, 20, '2026-11-01', 1, '2026-10-22 01:10:40'),
(12, 'Amlodipine 5mg',       'Pfizer',       'Tablet',    95.00,  104, 35, '2027-02-28', 3, '2026-10-22 01:10:40');

-- --------------------------------------------------------
-- sales
-- --------------------------------------------------------
CREATE TABLE sales (
    sale_id      INT AUTO_INCREMENT PRIMARY KEY,
    sale_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    user_id      INT,
    CONSTRAINT fk_sale_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

INSERT INTO sales (sale_id, sale_date, total_amount, user_id) VALUES
(16, '2026-09-20 09:15:00', 180.50, 2),
(17, '2026-09-25 10:00:00', 455.25, 2),
(18, '2026-10-22 18:17:03', 280.00, 2),
(19, '2026-10-22 19:28:02', 320.00, 2),
(20, '2026-10-23 02:15:22', 519.75, 2);

-- --------------------------------------------------------
-- sale_items
-- --------------------------------------------------------
CREATE TABLE sale_items (
    sale_item_id  INT AUTO_INCREMENT PRIMARY KEY,
    sale_id       INT NOT NULL,
    medicine_id   INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_si_sale
        FOREIGN KEY (sale_id) REFERENCES sales(sale_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_si_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO sale_items (sale_item_id, sale_id, medicine_id, quantity_sold, price_at_sale) VALUES
(41, 18, 12, 1, 95.00),
(42, 18,  3, 1, 65.00),
(43, 18,  7, 1,120.00),
(44, 19, 12, 2, 95.00),
(45, 19,  3, 2, 65.00),
(46, 20, 12, 3, 95.00),
(47, 20, 10, 3, 78.25);

-- Reset AUTO_INCREMENT values to continue from current max IDs
ALTER TABLE users      AUTO_INCREMENT = 4;
ALTER TABLE suppliers  AUTO_INCREMENT = 15;
ALTER TABLE medicines  AUTO_INCREMENT = 13;
ALTER TABLE sales      AUTO_INCREMENT = 21;
ALTER TABLE sale_items AUTO_INCREMENT = 48;

SELECT 'pims_db script completed successfully.' AS Status;