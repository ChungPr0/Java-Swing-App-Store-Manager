DROP DATABASE QuanLyBanHang;
CREATE DATABASE QuanLyBanHang;
USE QuanLyBanHang;

CREATE TABLE Suppliers (
    sup_ID INT PRIMARY KEY AUTO_INCREMENT,
    sup_name NVARCHAR(100) NOT NULL,
    sup_address NVARCHAR(255),
    sup_phone VARCHAR(20)
);

CREATE TABLE Customers (
    cus_ID INT PRIMARY KEY AUTO_INCREMENT,
    cus_name NVARCHAR(100) NOT NULL,
    cus_address NVARCHAR(255),
    cus_phone VARCHAR(20)
);

CREATE TABLE Staffs (
    sta_ID INT PRIMARY KEY AUTO_INCREMENT,
    sta_name NVARCHAR(100) NOT NULL,
    sta_date_of_birth DATE,
    sta_phone VARCHAR(20),
    sta_address NVARCHAR(255)
);

CREATE TABLE ProductTypes (
    type_ID INT PRIMARY KEY AUTO_INCREMENT,
    type_name NVARCHAR(100) NOT NULL
);

CREATE TABLE Products (
    pro_ID INT PRIMARY KEY AUTO_INCREMENT,
    pro_name NVARCHAR(100) NOT NULL,
    pro_price DECIMAL(18, 2) NOT NULL, -- Dùng Decimal cho tiền tệ
    pro_count INT DEFAULT 0,
    type_ID INT NOT NULL,
    sup_ID INT,
    FOREIGN KEY (sup_ID) REFERENCES Suppliers(sup_ID),
    FOREIGN KEY (type_ID) REFERENCES ProductTypes(type_ID)
);

CREATE TABLE Invoices (
    inv_ID INT PRIMARY KEY AUTO_INCREMENT,
    sta_ID INT,
    cus_ID INT,
    inv_price DECIMAL(18, 2),
    FOREIGN KEY (sta_ID) REFERENCES Staffs(sta_ID),
    FOREIGN KEY (cus_ID) REFERENCES Customers(cus_ID)
);

CREATE TABLE Invoice_details (
    ind_ID INT PRIMARY KEY AUTO_INCREMENT,
    inv_ID INT,
    pro_ID INT,
    ind_count INT NOT NULL,
    FOREIGN KEY (inv_ID) REFERENCES Invoices(inv_ID),
    FOREIGN KEY (pro_ID) REFERENCES Products(pro_ID)
);

CREATE TABLE Accounts (
    acc_ID INT PRIMARY KEY AUTO_INCREMENT,
    acc_name varchar(20),
    acc_pass varchar(255)
);

INSERT INTO Accounts (acc_name, acc_pass)
VALUES ('admin', '12345678');

INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address) VALUES
(N'Trần Thị Hoa', '1992-08-21', '0902345678', N'Hồ Chí Minh'),
(N'Lê Văn Bình', '1988-12-05', '0913456789', N'Đà Nẵng'),
(N'Phạm Thu Trang', '1995-03-18', '0924567890', N'Hải Phòng'),
(N'Hoàng Đức Minh', '1991-11-10', '0935678901', N'Cần Thơ'),
(N'Vũ Thị Hạnh', '1993-07-22', '0946789012', N'Huế'),
(N'Đỗ Ngọc Tuấn', '1989-02-14', '0957890123', N'Quảng Ninh'),
(N'Bùi Thị Mai', '1996-09-30', '0968901234', N'Nha Trang'),
(N'Nguyễn Anh Dũng', '1994-04-25', '0979012345', N'Buôn Ma Thuột'),
(N'Đặng Thị Ngọc', '1990-01-19', '0980123456', N'Vũng Tàu'),
(N'Phan Văn Khánh', '1987-06-11', '0901122334', N'Nam Định'),
(N'Dương Thị Kim', '1998-10-09', '0912233445', N'Thanh Hóa'),
(N'Tạ Minh Hoàng', '1993-12-28', '0923344556', N'Hải Dương'),
(N'Nguyễn Quỳnh Chi', '1997-07-07', '0934455667', N'Thái Bình'),
(N'Hồ Văn Phúc', '1992-03-03', '0945566778', N'Hà Tĩnh'),
(N'Võ Thị Loan', '1995-05-20', '0956677889', N'Quảng Nam'),
(N'Lý Thành Đạt', '1989-09-17', '0967788990', N'Lào Cai'),
(N'Cao Thị Yến', '1996-11-12', '0978899001', N'Phú Thọ'),
(N'Ngô Văn Tài', '1991-01-29', '0989900112', N'Gia Lai'),
(N'Đinh Thị Hòa', '1988-04-04', '0902211334', N'Kon Tum'),
(N'Phùng Minh Khang', '1993-06-26', '0913322445', N'Sóc Trăng'),
(N'Mai Thị Ngân', '1997-02-18', '0924433556', N'Trà Vinh'),
(N'Đoàn Văn Huy', '1990-08-14', '0935544667', N'Bạc Liêu'),
(N'Kiều Thị Phương', '1994-09-23', '0946655778', N'Long An'),
(N'Hoàng Gia Bảo', '1995-01-06', '0957766889', N'Quảng Bình'),
(N'Vũ Thị Thu', '1992-12-30', '0968877990', N'Hòa Bình'),
(N'Trịnh Văn Toàn', '1989-05-09', '0979988001', N'Lạng Sơn'),
(N'Chu Thị Nhung', '1998-07-15', '0980099112', N'Tuyên Quang'),
(N'Trần Minh Quân', '1991-03-27', '0903344556', N'Yên Bái'),
(N'Lã Thị Hương', '1996-10-08', '0914455667', N'Bắc Giang');

INSERT INTO Suppliers (sup_name, sup_address, sup_phone) VALUES
(N'Công ty Công Nghệ Việt', N'Hà Nội', '0901111111'),
(N'Đại Lý Phân Phối Á Châu', N'TP.HCM', '0902222222'),
(N'Linh Kiện PC Giá Rẻ', N'Đà Nẵng', '0903333333'),
(N'Thế Giới Di Động Xanh', N'Hà Nội', '0904444444'),
(N'Công ty TNHH Tuấn Minh', N'Hải Phòng', '0905555555'),
(N'Kho Hàng Tổng Hợp', N'Cần Thơ', '0906666666'),
(N'Nhà Phân Phối Bảo An', N'TP.HCM', '0907777777'),
(N'Công Nghệ Số Hùng Cường', N'Hà Nội', '0908888888'),
(N'Vi tính Ngôi Sao Mới', N'TP.HCM', '0909999999'),
(N'Phụ Kiện Chính Hãng', N'Đà Nẵng', '0910000000'),
(N'GearVN Chi Nhánh 2', N'Hà Nội', '0911111111'),
(N'Hanoicomputer Sài Gòn', N'TP.HCM', '0912222222'),
(N'An Phát PC', N'Hà Nội', '0913333333'),
(N'Phúc Anh Smart World', N'Hà Nội', '0914444444'),
(N'MemoryZone', N'TP.HCM', '0915555555'),
(N'Laptop88', N'Hà Nội', '0916666666'),
(N'ThinkPro Việt Nam', N'TP.HCM', '0917777777'),
(N'Xgear Gaming', N'Hà Nội', '0918888888'),
(N'Phong Vũ Computer', N'TP.HCM', '0919999999'),
(N'Nguyễn Kim Tech', N'Đà Nẵng', '0920000000'),
(N'CellphoneS Phụ Kiện', N'Hà Nội', '0921111111'),
(N'Hoàng Hà Mobile', N'TP.HCM', '0922222222'),
(N'Didongviet', N'Hà Nội', '0923333333'),
(N'Shop Dunk', N'TP.HCM', '0924444444'),
(N'FPT Shop Linh Kiện', N'Đà Nẵng', '0925555555'),
(N'Viettel Store', N'Hà Nội', '0926666666'),
(N'Thế Giới Số 360', N'TP.HCM', '0927777777'),
(N'TechOne', N'Hà Nội', '0928888888'),
(N'ClickBuy', N'TP.HCM', '0929999999'),
(N'Bạch Long Mobile', N'TP.HCM', '0930000000');

INSERT INTO ProductTypes (type_name) VALUES
(N'Laptop / Máy tính xách tay'),
(N'Linh kiện máy tính'),
(N'Phụ kiện (Chuột, Phím, Tai nghe)'),
(N'Màn hình'),
(N'Thiết bị mạng');


INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID) VALUES
-- Laptop (Loại 1)
(N'MacBook Air M1', 18500000, 15, 1, 1),
(N'MacBook Pro M2', 30000000, 10, 1, 2),
(N'Laptop Dell XPS 13', 25000000, 5, 1, 3),
(N'Laptop HP Envy 13', 21000000, 8, 1, 4),
(N'Laptop Asus ROG Strix', 28000000, 12, 1, 5),
(N'Laptop Lenovo Legion 5', 26500000, 20, 1, 6),
(N'Laptop Acer Nitro 5', 19000000, 25, 1, 7),
(N'Laptop MSI Gaming', 22000000, 10, 1, 8),
(N'Surface Pro 9', 24000000, 7, 1, 9),
(N'Laptop LG Gram', 27000000, 5, 1, 10),

-- Linh kiện & Phụ kiện (Loại 2)
(N'Chuột Razer DeathAdder', 1200000, 50, 2, 1),
(N'Bàn phím cơ Keychron K2', 1800000, 30, 2, 2),
(N'Tai nghe Sony WH-1000XM5', 6500000, 15, 2, 3),
(N'Màn hình LG 27 inch 4K', 8000000, 10, 2, 4),
(N'Màn hình Dell Ultrasharp', 9500000, 8, 2, 5),
(N'RAM Kingston Fury 16GB', 1100000, 100, 2, 6),
(N'SSD Samsung 980 Pro 1TB', 2500000, 60, 2, 7),
(N'VGA RTX 3060 Ti', 9000000, 5, 2, 8),
(N'CPU Intel Core i5 13600K', 7500000, 10, 2, 9),
(N'CPU AMD Ryzen 7 7800X3D', 10500000, 8, 2, 10),

-- Phụ kiện khác (Loại 3)
(N'Webcam Logitech C920', 1500000, 40, 2, 1),
(N'Loa Bluetooth JBL Flip 6', 2300000, 25, 2, 2),
(N'Bàn di chuột SteelSeries', 450000, 80, 2, 3),
(N'Giá đỡ Laptop nhôm', 350000, 60, 2, 4),
(N'Hub chuyển đổi Type-C', 550000, 45, 2, 5),
(N'Dây cáp HDMI 2.1', 200000, 100, 2, 6),
(N'Sạc dự phòng Anker 20000', 1200000, 35, 2, 7),
(N'Bàn phím Akko 3098B', 1600000, 20, 2, 8),
(N'Chuột Logitech MX Master 3S', 2200000, 18, 2, 9),
(N'Tai nghe Gaming HyperX', 1800000, 22, 2, 10);

INSERT INTO Customers (cus_name, cus_phone, cus_address) VALUES
(N'Phạm Minh Tuấn', '0988777666', N'Hà Nội'),
(N'Lê Thu Thảo', '0977666555', N'TP.HCM'),
(N'Đặng Hùng Dũng', '0966555444', N'Đà Nẵng'),
(N'Hoàng Mai Anh', '0955444333', N'Cần Thơ'),
(N'Vũ Đức Thắng', '0944333222', N'Hải Phòng');

INSERT INTO Invoices (sta_ID, cus_ID, inv_price) VALUES
(1, 1, 18500000),
(2, 2, 250000),
(1, 3, 30000000),
(2, 4, 15000000),
(1, 5, 800000),
(2, 1, 45000000),
(1, 2, 1200000),
(2, 3, 21000000),
(1, 4, 5500000),
(2, 5, 28000000);

INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 2, 1),
(4, 3, 1),
(5, 3, 1),
(6, 3, 1),
(7, 1, 1),
(8, 4, 1),
(9, 4, 2),
(10, 5, 1);