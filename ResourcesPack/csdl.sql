-- ==========================================
-- 1. XÓA BẢNG CŨ (NẾU TỒN TẠI)
-- ==========================================
PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS Invoice_details;
DROP TABLE IF EXISTS Invoices;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Discounts; -- Bảng mới
DROP TABLE IF EXISTS ProductTypes;
DROP TABLE IF EXISTS Staffs;
DROP TABLE IF EXISTS Customers;
DROP TABLE IF EXISTS Suppliers;

PRAGMA foreign_keys = ON;

-- ==========================================
-- 2. TẠO CẤU TRÚC BẢNG MỚI
-- ==========================================

-- Bảng Nhà cung cấp
CREATE TABLE Suppliers (
    sup_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    sup_name TEXT NOT NULL,
    sup_address TEXT NOT NULL,
    sup_phone TEXT NOT NULL,
    sup_start_date TEXT DEFAULT (DATE('now')),
    sup_description TEXT NOT NULL
);

-- Bảng Khách hàng
CREATE TABLE Customers (
    cus_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    cus_name TEXT NOT NULL,
    cus_address TEXT,
    cus_phone TEXT
);

-- Bảng Nhân viên (Quan trọng để đăng nhập)
CREATE TABLE Staffs (
    sta_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    sta_name TEXT NOT NULL,
    sta_date_of_birth TEXT NOT NULL,
    sta_phone TEXT NOT NULL,
    sta_address TEXT NOT NULL,
    sta_salary REAL DEFAULT 5000000,
    sta_start_date TEXT DEFAULT (DATE('now')),
    sta_username TEXT UNIQUE,
    sta_password TEXT,
    sta_role TEXT DEFAULT 'Staff' -- 'Admin' hoặc 'Staff'
);

-- Bảng Loại sản phẩm
CREATE TABLE ProductTypes (
    type_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    type_name TEXT NOT NULL UNIQUE
);

-- Bảng Sản phẩm
CREATE TABLE Products (
    pro_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    pro_name TEXT NOT NULL,
    pro_price REAL NOT NULL,
    pro_count INTEGER DEFAULT 0,
    pro_description TEXT,
    type_ID INTEGER,
    sup_ID INTEGER,
    FOREIGN KEY (type_ID) REFERENCES ProductTypes(type_ID),
    FOREIGN KEY (sup_ID) REFERENCES Suppliers(sup_ID)
);

-- Bảng Mã giảm giá (MỚI)
CREATE TABLE Discounts (
    dis_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    dis_code TEXT NOT NULL UNIQUE,
    dis_type TEXT NOT NULL, -- 'PERCENT' (%) hoặc 'FIXED' (Tiền mặt)
    dis_value REAL NOT NULL,
    dis_scope TEXT NOT NULL, -- 'ALL' (Toàn bộ) hoặc 'CATEGORY' (Theo loại)
    dis_category_id INTEGER, -- Null nếu scope là ALL
    dis_start_date TEXT,
    dis_end_date TEXT,
    FOREIGN KEY (dis_category_id) REFERENCES ProductTypes(type_ID)
);

-- Bảng Hóa đơn (Cập nhật thêm cột dis_ID)
CREATE TABLE Invoices (
    inv_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    sta_ID INTEGER,
    cus_ID INTEGER,
    inv_price REAL DEFAULT 0,
    inv_date TEXT DEFAULT (DATETIME('now')),
    dis_ID INTEGER, -- Lưu mã giảm giá đã dùng (Có thể NULL)
    FOREIGN KEY (sta_ID) REFERENCES Staffs(sta_ID),
    FOREIGN KEY (cus_ID) REFERENCES Customers(cus_ID),
    FOREIGN KEY (dis_ID) REFERENCES Discounts(dis_ID)
);

-- Bảng Chi tiết hóa đơn
CREATE TABLE Invoice_details (
    ind_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    inv_ID INTEGER,
    pro_ID INTEGER,
    ind_count INTEGER NOT NULL,
    unit_price REAL DEFAULT 0,
    FOREIGN KEY (inv_ID) REFERENCES Invoices(inv_ID) ON DELETE CASCADE,
    FOREIGN KEY (pro_ID) REFERENCES Products(pro_ID)
);