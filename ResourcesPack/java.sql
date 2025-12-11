-- 1. Tạo cơ sở dữ liệu
CREATE DATABASE QuanLyBanHang;
USE QuanLyBanHang;

-- 2. Tạo bảng NHÀ CUNG CẤP (Suppliers) trước vì Product tham chiếu đến nó
CREATE TABLE Suppliers (
    sup_ID INT PRIMARY KEY AUTO_INCREMENT,
    sup_name NVARCHAR(100) NOT NULL,
    sup_address NVARCHAR(255),
    sup_phone VARCHAR(20)
);

-- 3. Tạo bảng KHÁCH HÀNG (Customers)
CREATE TABLE Customers (
    cus_ID INT PRIMARY KEY AUTO_INCREMENT,
    cus_name NVARCHAR(100) NOT NULL,
    cus_address NVARCHAR(255),
    cus_phone VARCHAR(20)
);

-- 4. Tạo bảng NHÂN VIÊN (Staffs)
CREATE TABLE Staffs (
    sta_ID INT PRIMARY KEY AUTO_INCREMENT,
    sta_name NVARCHAR(100) NOT NULL,
    sta_date_of_birth DATE,
    sta_phone VARCHAR(20),
    sta_address NVARCHAR(255)
);

-- 5. Tạo bảng SẢN PHẨM (Products) - Tham chiếu đến Suppliers
CREATE TABLE Products (
    pro_ID INT PRIMARY KEY AUTO_INCREMENT,
    pro_name NVARCHAR(100) NOT NULL,
    pro_price DECIMAL(18, 2) NOT NULL, -- Dùng Decimal cho tiền tệ
    pro_count INT DEFAULT 0,
    pro_type INT NOT NULL,
    sup_ID INT,
    FOREIGN KEY (sup_ID) REFERENCES Suppliers(sup_ID)
);

-- 6. Tạo bảng HÓA ĐƠN (Invoices) - Tham chiếu Staffs và Customers
CREATE TABLE Invoices (
    inv_ID INT PRIMARY KEY AUTO_INCREMENT,
    sta_ID INT,
    cus_ID INT,
    inv_price DECIMAL(18, 2),
    FOREIGN KEY (sta_ID) REFERENCES Staffs(sta_ID),
    FOREIGN KEY (cus_ID) REFERENCES Customers(cus_ID)
);

-- 7. Tạo bảng CHI TIẾT HÓA ĐƠN (Invoice_details) - Tham chiếu Invoices và Products
CREATE TABLE Invoice_details (
    ind_ID INT PRIMARY KEY AUTO_INCREMENT,
    inv_ID INT,
    pro_ID INT,
    ind_count INT NOT NULL,
    FOREIGN KEY (inv_ID) REFERENCES Invoices(inv_ID),
    FOREIGN KEY (pro_ID) REFERENCES Products(pro_ID)
);

-- 8. Tạo bảng Accounts
CREATE TABLE Accounts (
    acc_ID INT PRIMARY KEY AUTO_INCREMENT,
    acc_name varchar(20),
    acc_pass varchar(255)
);

INSERT INTO Accounts (acc_name, acc_pass) 
VALUES ('admin', '12345678');

INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address)
VALUES 
(N'Nguyễn Văn An', '1990-05-15', '0901234567', 'hanoi')