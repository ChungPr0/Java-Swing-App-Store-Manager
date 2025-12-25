-- =====================
-- 1. THIẾT LẬP (SQLite không cần tạo DB bằng lệnh, chỉ cần PRAGMA)
-- =====================
PRAGMA foreign_keys = OFF; -- Tắt kiểm tra khóa ngoại để insert nhanh

-- ====================
-- 2. TẠO CẤU TRÚC BẢNG (Chuẩn SQLite)
-- ====================

DROP TABLE IF EXISTS Invoice_details;
DROP TABLE IF EXISTS Invoices;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS ProductTypes;
DROP TABLE IF EXISTS Staffs;
DROP TABLE IF EXISTS Customers;
DROP TABLE IF EXISTS Suppliers;

CREATE TABLE Suppliers (
    sup_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    sup_name TEXT NOT NULL,
    sup_address TEXT NOT NULL,
    sup_phone TEXT NOT NULL,
    sup_start_date TEXT DEFAULT (DATE('now')),
    sup_description TEXT NOT NULL
);

CREATE TABLE Customers (
    cus_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    cus_name TEXT NOT NULL,
    cus_address TEXT,
    cus_phone TEXT NOT NULL
);

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
    sta_role TEXT DEFAULT 'Staff'
);

CREATE TABLE ProductTypes (
    type_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    type_name TEXT NOT NULL UNIQUE
);

CREATE TABLE Products (
    pro_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    pro_name TEXT NOT NULL,
    pro_price REAL NOT NULL,
    pro_count INTEGER DEFAULT 0,
    type_ID INTEGER,
    sup_ID INTEGER,
    FOREIGN KEY (type_ID) REFERENCES ProductTypes(type_ID),
    FOREIGN KEY (sup_ID) REFERENCES Suppliers(sup_ID)
);

CREATE TABLE Invoices (
    inv_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    sta_ID INTEGER,
    cus_ID INTEGER,
    inv_price REAL DEFAULT 0,
    inv_date TEXT DEFAULT (DATETIME('now')),
    FOREIGN KEY (sta_ID) REFERENCES Staffs(sta_ID),
    FOREIGN KEY (cus_ID) REFERENCES Customers(cus_ID)
);

CREATE TABLE Invoice_details (
    ind_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    inv_ID INTEGER,
    pro_ID INTEGER,
    ind_count INTEGER NOT NULL,
    unit_price REAL DEFAULT 0,
    FOREIGN KEY (inv_ID) REFERENCES Invoices(inv_ID) ON DELETE CASCADE,
    FOREIGN KEY (pro_ID) REFERENCES Products(pro_ID)
);

-- ===============
-- 3. CHÈN DỮ LIỆU
-- ===============

-- 3.1. NHÀ CUNG CẤP
INSERT INTO Suppliers (sup_name, sup_address, sup_phone, sup_start_date, sup_description) VALUES
('Dell Việt Nam', 'Hà Nội', '1800545455', '2015-05-12', 'Đối tác phân phối chính thức các dòng Laptop Dell Inspiron, Vostro và XPS.'),
('Asus Corp', 'TP.HCM', '1900555581', '2016-08-20', 'Nhà cung cấp Laptop, Mainboard, VGA Asus và các sản phẩm ROG Gaming.'),
('Samsung Vina', 'Bắc Ninh', '0988777666', '2015-11-10', 'Đối tác chiến lược mảng màn hình máy tính, SSD và RAM Samsung.'),
('Apple Distributor', 'TP.HCM', '02833334444', '2019-09-05', 'Nhà phân phối ủy quyền Apple tại Việt Nam (Macbook, iMac, Phụ kiện).'),
('HP Việt Nam', 'Hà Nội', '18006688', '2016-07-30', 'Cung cấp giải pháp máy tính doanh nghiệp, Laptop văn phòng và máy in HP.'),
('Lenovo Group', 'Đà Nẵng', '0236123456', '2018-12-12', 'Chuyên phân phối các dòng ThinkPad, Legion Gaming và thiết bị văn phòng.'),
('Sony Electronics', 'TP.HCM', '1800588885', '2015-06-25', 'Nhà cung cấp tai nghe, loa và các thiết bị âm thanh chất lượng cao.'),
('LG Việt Nam', 'Hà Nội', '18001503', '2017-02-14', 'Đối tác cung cấp màn hình LG Gram, màn hình UltraWide và giải pháp hiển thị.'),
('MSI Gaming', 'TP.HCM', '02877778888', '2018-08-08', 'Chuyên Laptop Gaming, Bo mạch chủ, Card đồ họa và Gear MSI.'),
('Gigabyte VN', 'Hà Nội', '02433332222', '2019-04-18', 'Nhà cung cấp phần cứng máy tính: Mainboard, VGA Gigabyte và Aorus.'),
('Kingston Tech', 'TP.HCM', '02899990000', '2016-10-01', 'Nhà phân phối RAM, SSD, USB và thẻ nhớ Kingston chính hãng.'),
('Logitech VN', 'TP.HCM', '02811112222', '2020-05-20', 'Cung cấp chuột, bàn phím, webcam và thiết bị hội nghị truyền hình.'),
('Intel VN', 'TP.HCM', '02855556666', '2015-01-01', 'Nhà cung cấp vi xử lý (CPU) Intel Core, Xeon và các giải pháp máy chủ.'),
('AMD VN', 'Hà Nội', '02488889999', '2017-11-11', 'Cung cấp CPU Ryzen và Card đồ họa Radeon chính hãng.'),
('Western Digital', 'TP.HCM', '1800555555', '2016-09-09', 'Chuyên các giải pháp lưu trữ: HDD, SSD WD Blue, Black, Red.'),
('Seagate VN', 'Hà Nội', '1800888888', '2018-03-03', 'Cung cấp ổ cứng HDD Seagate Barracuda, IronWolf và SkyHawk.'),
('TP-Link VN', 'TP.HCM', '02866667777', '2019-07-27', 'Nhà cung cấp thiết bị mạng: Router Wi-Fi, Switch, Camera Tapo.'),
('Canon Marketing', 'TP.HCM', '02838200466', '2015-12-25', 'Cung cấp máy in, máy scan và mực in chính hãng Canon cho văn phòng.'),
('FPT Trading', 'Hà Nội', '02473008888', '2010-10-10', 'Nhà phân phối tổng hợp các sản phẩm CNTT uy tín lâu năm.'),
('Digiworld', 'TP.HCM', '02839290059', '2012-02-20', 'Nhà phân phối ICT hàng đầu, cung cấp đa dạng thương hiệu công nghệ.');

-- Chèn thêm 80 Nhà cung cấp
INSERT INTO Suppliers (sup_name, sup_address, sup_phone, sup_start_date, sup_description) VALUES
('Nhà Cung Cấp 21', 'Khu công nghiệp Tân Bình, TP.HCM', '0900000021', '2020-01-15', 'Chuyên cung cấp linh kiện điện tử sỉ lẻ.'),
('Nhà Cung Cấp 22', 'Quận Cầu Giấy, Hà Nội', '0900000022', '2021-03-22', 'Đối tác cung cấp phụ kiện máy tính giá rẻ.'),
('Nhà Cung Cấp 23', 'Quận Hải Châu, Đà Nẵng', '0900000023', '2019-11-05', 'Nhà phân phối cáp kết nối và bộ chuyển đổi.'),
('Nhà Cung Cấp 24', 'Khu Công Nghệ Cao, TP.HCM', '0900000024', '2022-06-10', 'Cung cấp giải pháp tản nhiệt nước cho PC.'),
('Nhà Cung Cấp 25', 'Quận 1, TP.HCM', '0900000025', '2018-09-09', 'Đơn vị nhập khẩu vỏ case máy tính cao cấp.'),
('Nhà Cung Cấp 26', 'Quận Đống Đa, Hà Nội', '0900000026', '2020-12-25', 'Chuyên cung cấp ghế Gaming và bàn làm việc.'),
('Nhà Cung Cấp 27', 'Thành phố Thủ Đức, TP.HCM', '0900000027', '2021-02-14', 'Nhà cung cấp thiết bị Smart Home.'),
('Nhà Cung Cấp 28', 'Quận 7, TP.HCM', '0900000028', '2019-07-30', 'Phân phối phần mềm bản quyền Microsoft.'),
('Nhà Cung Cấp 29', 'Quận Ba Đình, Hà Nội', '0900000029', '2017-05-20', 'Cung cấp thiết bị văn phòng phẩm.'),
('Nhà Cung Cấp 30', 'Bình Dương', '0900000030', '2022-01-01', 'Xưởng lắp ráp máy tính đồng bộ.'),
('Nhà Cung Cấp 31', 'Đồng Nai', '0900000031', '2020-08-15', 'Cung cấp linh kiện thay thế cho Laptop.'),
('Nhà Cung Cấp 32', 'Cần Thơ', '0900000032', '2021-10-10', 'Nhà phân phối camera quan sát khu vực miền Tây.'),
('Nhà Cung Cấp 33', 'Hải Phòng', '0900000033', '2019-04-22', 'Cung cấp thiết bị mạng công nghiệp.'),
('Nhà Cung Cấp 34', 'Quận 10, TP.HCM', '0900000034', '2023-01-11', 'Đối tác mới chuyên về màn hình di động.'),
('Nhà Cung Cấp 35', 'Quận Thanh Xuân, Hà Nội', '0900000035', '2018-06-06', 'Cung cấp mực in và giấy in số lượng lớn.'),
('Nhà Cung Cấp 36', 'Nha Trang', '0900000036', '2020-03-03', 'Chi nhánh phân phối miền Trung.'),
('Nhà Cung Cấp 37', 'Vũng Tàu', '0900000037', '2021-09-19', 'Cung cấp thiết bị lưu trữ NAS.'),
('Nhà Cung Cấp 38', 'Quận 3, TP.HCM', '0900000038', '2017-12-12', 'Chuyên các dòng máy trạm Workstation.'),
('Nhà Cung Cấp 39', 'Quận Hoàng Mai, Hà Nội', '0900000039', '2022-05-05', 'Nhà cung cấp loa vi tính và Soundbar.'),
('Nhà Cung Cấp 40', 'Quận Gò Vấp, TP.HCM', '0900000040', '2019-11-20', 'Cung cấp balo, túi chống sốc Laptop.'),
('Nhà Cung Cấp 41', 'Địa chỉ 41', '0900000041', '2020-02-28', 'Nhà cung cấp linh kiện tổng hợp.'),
('Nhà Cung Cấp 42', 'Địa chỉ 42', '0900000042', '2021-07-07', 'Đối tác cung cấp dịch vụ bảo hành.'),
('Nhà Cung Cấp 43', 'Địa chỉ 43', '0900000043', '2018-01-01', 'Cung cấp ốc vít, dụng cụ sửa chữa.'),
('Nhà Cung Cấp 44', 'Địa chỉ 44', '0900000044', '2022-08-15', 'Nhà phân phối keo tản nhiệt.'),
('Nhà Cung Cấp 45', 'Địa chỉ 45', '0900000045', '2019-05-10', 'Cung cấp đèn LED trang trí PC.'),
('Nhà Cung Cấp 46', 'Địa chỉ 46', '0900000046', '2020-10-20', 'Chuyên cung cấp Pad chuột gaming.'),
('Nhà Cung Cấp 47', 'Địa chỉ 47', '0900000047', '2021-04-30', 'Đối tác vận chuyển logistics.'),
('Nhà Cung Cấp 48', 'Địa chỉ 48', '0900000048', '2017-09-15', 'Cung cấp thùng carton đóng gói.'),
('Nhà Cung Cấp 49', 'Địa chỉ 49', '0900000049', '2022-12-01', 'Nhà cung cấp tem bảo hành.'),
('Nhà Cung Cấp 50', 'Địa chỉ 50', '0900000050', '2018-03-25', 'Cung cấp dây nguồn máy tính.'),
('Nhà Cung Cấp 51', 'Địa chỉ 51', '0900000051', '2020-06-18', 'Đối tác nhập khẩu trực tiếp.'),
('Nhà Cung Cấp 52', 'Địa chỉ 52', '0900000052', '2021-11-11', 'Đại lý cấp 1 khu vực phía Nam.'),
('Nhà Cung Cấp 53', 'Địa chỉ 53', '0900000053', '2019-02-02', 'Đại lý cấp 1 khu vực phía Bắc.'),
('Nhà Cung Cấp 54', 'Địa chỉ 54', '0900000054', '2023-03-03', 'Nhà cung cấp startup công nghệ.'),
('Nhà Cung Cấp 55', 'Địa chỉ 55', '0900000055', '2017-08-08', 'Cung cấp giải pháp phần mềm.'),
('Nhà Cung Cấp 56', 'Địa chỉ 56', '0900000056', '2022-09-09', 'Cung cấp thiết bị hội nghị.'),
('Nhà Cung Cấp 57', 'Địa chỉ 57', '0900000057', '2020-01-20', 'Chuyên cung cấp máy chiếu.'),
('Nhà Cung Cấp 58', 'Địa chỉ 58', '0900000058', '2021-05-15', 'Nhà phân phối màn hình quảng cáo.'),
('Nhà Cung Cấp 59', 'Địa chỉ 59', '0900000059', '2018-10-10', 'Cung cấp máy hủy tài liệu.'),
('Nhà Cung Cấp 60', 'Địa chỉ 60', '0900000060', '2022-04-04', 'Cung cấp máy đếm tiền.'),
('Nhà Cung Cấp 61', 'Địa chỉ 61', '0900000061', '2019-07-07', 'Nhà cung cấp thiết bị POS.'),
('Nhà Cung Cấp 62', 'Địa chỉ 62', '0900000062', '2020-12-12', 'Cung cấp máy in hóa đơn.'),
('Nhà Cung Cấp 63', 'Địa chỉ 63', '0900000063', '2021-06-06', 'Cung cấp máy quét mã vạch.'),
('Nhà Cung Cấp 64', 'Địa chỉ 64', '0900000064', '2017-03-15', 'Đối tác cung cấp giấy in nhiệt.'),
('Nhà Cung Cấp 65', 'Địa chỉ 65', '0900000065', '2022-11-20', 'Chuyên cung cấp ngăn kéo đựng tiền.'),
('Nhà Cung Cấp 66', 'Địa chỉ 66', '0900000066', '2018-05-05', 'Nhà cung cấp phụ kiện mạng.'),
('Nhà Cung Cấp 67', 'Địa chỉ 67', '0900000067', '2020-09-09', 'Cung cấp kìm bấm mạng, hạt mạng.'),
('Nhà Cung Cấp 68', 'Địa chỉ 68', '0900000068', '2021-02-22', 'Cung cấp cáp quang.'),
('Nhà Cung Cấp 69', 'Địa chỉ 69', '0900000069', '2019-12-30', 'Đối tác thi công phòng Net.'),
('Nhà Cung Cấp 70', 'Địa chỉ 70', '0900000070', '2023-01-15', 'Cung cấp bàn ghế Cyber Game.'),
('Nhà Cung Cấp 71', 'Địa chỉ 71', '0900000071', '2017-06-20', 'Nhà cung cấp Server Bootrom.'),
('Nhà Cung Cấp 72', 'Địa chỉ 72', '0900000072', '2022-08-08', 'Cung cấp Switch 24 port.'),
('Nhà Cung Cấp 73', 'Địa chỉ 73', '0900000073', '2020-04-10', 'Nhà phân phối UPS bộ lưu điện.'),
('Nhà Cung Cấp 74', 'Địa chỉ 74', '0900000074', '2021-10-25', 'Cung cấp ắc quy cho UPS.'),
('Nhà Cung Cấp 75', 'Địa chỉ 75', '0900000075', '2018-11-11', 'Dịch vụ bảo trì hệ thống.'),
('Nhà Cung Cấp 76', 'Địa chỉ 76', '0900000076', '2022-03-18', 'Cung cấp phần mềm diệt virus.'),
('Nhà Cung Cấp 77', 'Địa chỉ 77', '0900000077', '2019-08-20', 'Đại lý Kaspersky.'),
('Nhà Cung Cấp 78', 'Địa chỉ 78', '0900000078', '2020-12-05', 'Đại lý BKAV.'),
('Nhà Cung Cấp 79', 'Địa chỉ 79', '0900000079', '2021-07-22', 'Cung cấp Key Windows bản quyền.'),
('Nhà Cung Cấp 80', 'Địa chỉ 80', '0900000080', '2017-02-28', 'Cung cấp Key Office bản quyền.'),
('Nhà Cung Cấp 81', 'Địa chỉ 81', '0900000081', '2022-09-15', 'Nhà cung cấp giải pháp Cloud.'),
('Nhà Cung Cấp 82', 'Địa chỉ 82', '0900000082', '2020-05-30', 'Dịch vụ Hosting và Domain.'),
('Nhà Cung Cấp 83', 'Địa chỉ 83', '0900000083', '2021-01-10', 'Thiết kế Website bán hàng.'),
('Nhà Cung Cấp 84', 'Địa chỉ 84', '0900000084', '2018-07-07', 'Cung cấp dịch vụ Email doanh nghiệp.'),
('Nhà Cung Cấp 85', 'Địa chỉ 85', '0900000085', '2023-02-14', 'Giải pháp tổng đài ảo VoIP.'),
('Nhà Cung Cấp 86', 'Địa chỉ 86', '0900000086', '2019-10-25', 'Cung cấp tai nghe Call Center.'),
('Nhà Cung Cấp 87', 'Địa chỉ 87', '0900000087', '2020-11-11', 'Nhà cung cấp điện thoại bàn IP.'),
('Nhà Cung Cấp 88', 'Địa chỉ 88', '0900000088', '2021-09-09', 'Cung cấp thiết bị họp trực tuyến.'),
('Nhà Cung Cấp 89', 'Địa chỉ 89', '0900000089', '2017-04-20', 'Nhà cung cấp Microphone thu âm.'),
('Nhà Cung Cấp 90', 'Địa chỉ 90', '0900000090', '2022-06-30', 'Cung cấp Webcam 4K.'),
('Nhà Cung Cấp 91', 'Địa chỉ 91', '0900000091', '2020-02-15', 'Cung cấp đèn Livestream.'),
('Nhà Cung Cấp 92', 'Địa chỉ 92', '0900000092', '2021-08-08', 'Cung cấp Capture Card.'),
('Nhà Cung Cấp 93', 'Địa chỉ 93', '0900000093', '2018-12-25', 'Thiết bị Stream Deck.'),
('Nhà Cung Cấp 94', 'Địa chỉ 94', '0900000094', '2023-01-01', 'Nhà cung cấp phông xanh quay phim.'),
('Nhà Cung Cấp 95', 'Địa chỉ 95', '0900000095', '2019-06-15', 'Cung cấp chân máy quay Tripod.'),
('Nhà Cung Cấp 96', 'Địa chỉ 96', '0900000096', '2020-08-30', 'Cung cấp Gimbal chống rung.'),
('Nhà Cung Cấp 97', 'Địa chỉ 97', '0900000097', '2021-12-10', 'Cung cấp thẻ nhớ máy ảnh.'),
('Nhà Cung Cấp 98', 'Địa chỉ 98', '0900000098', '2017-09-20', 'Nhà cung cấp pin sạc dự phòng.'),
('Nhà Cung Cấp 99', 'Địa chỉ 99', '0900000099', '2022-05-25', 'Cung cấp cáp sạc nhanh.'),
('Nhà Cung Cấp 100', 'Địa chỉ 100', '0900000100', '2020-03-15', 'Nhà cung cấp củ sạc GaN.');

-- 3.2. KHÁCH HÀNG
INSERT INTO Customers (cus_name, cus_address, cus_phone) VALUES
('Nguyễn Văn Khách 1', 'Ba Đình, Hà Nội', '0988111001'), ('Trần Thị Khách 2', 'Hoàn Kiếm, Hà Nội', '0988111002'), ('Lê Văn Khách 3', 'Đống Đa, Hà Nội', '0988111003'), ('Phạm Thị Khách 4', 'Cầu Giấy, Hà Nội', '0988111004'), ('Hoàng Văn Khách 5', 'Thanh Xuân, Hà Nội', '0988111005'),
('Vũ Thị Khách 6', 'Quận 1, TP.HCM', '0988111006'), ('Đặng Văn Khách 7', 'Quận 3, TP.HCM', '0988111007'), ('Bùi Thị Khách 8', 'Quận 5, TP.HCM', '0988111008'), ('Đỗ Văn Khách 9', 'Quận 7, TP.HCM', '0988111009'), ('Hồ Thị Khách 10', 'Thủ Đức, TP.HCM', '0988111010'),
('Ngô Văn Khách 11', 'Hải Châu, Đà Nẵng', '0988111011'), ('Dương Thị Khách 12', 'Sơn Trà, Đà Nẵng', '0988111012'), ('Lý Văn Khách 13', 'Ngũ Hành Sơn, Đà Nẵng', '0988111013'), ('Trương Thị Khách 14', 'Ninh Kiều, Cần Thơ', '0988111014'), ('Đinh Văn Khách 15', 'Hồng Bàng, Hải Phòng', '0988111015'),
('Lâm Thị Khách 16', 'Biên Hòa, Đồng Nai', '0988111016'), ('Mai Văn Khách 17', 'Thủ Dầu Một, Bình Dương', '0988111017'), ('Cao Thị Khách 18', 'Vũng Tàu', '0988111018'), ('Phan Văn Khách 19', 'Nha Trang, Khánh Hòa', '0988111019'), ('Hà Thị Khách 20', 'Buôn Ma Thuột, Đắk Lắk', '0988111020');

INSERT INTO Customers (cus_name, cus_address, cus_phone) VALUES
('Khách Hàng 21', 'Địa chỉ 21', '0911111021'), ('Khách Hàng 22', 'Địa chỉ 22', '0911111022'), ('Khách Hàng 23', 'Địa chỉ 23', '0911111023'), ('Khách Hàng 24', 'Địa chỉ 24', '0911111024'), ('Khách Hàng 25', 'Địa chỉ 25', '0911111025'),
('Khách Hàng 26', 'Địa chỉ 26', '0911111026'), ('Khách Hàng 27', 'Địa chỉ 27', '0911111027'), ('Khách Hàng 28', 'Địa chỉ 28', '0911111028'), ('Khách Hàng 29', 'Địa chỉ 29', '0911111029'), ('Khách Hàng 30', 'Địa chỉ 30', '0911111030'),
('Khách Hàng 31', 'Địa chỉ 31', '0911111031'), ('Khách Hàng 32', 'Địa chỉ 32', '0911111032'), ('Khách Hàng 33', 'Địa chỉ 33', '0911111033'), ('Khách Hàng 34', 'Địa chỉ 34', '0911111034'), ('Khách Hàng 35', 'Địa chỉ 35', '0911111035'),
('Khách Hàng 36', 'Địa chỉ 36', '0911111036'), ('Khách Hàng 37', 'Địa chỉ 37', '0911111037'), ('Khách Hàng 38', 'Địa chỉ 38', '0911111038'), ('Khách Hàng 39', 'Địa chỉ 39', '0911111039'), ('Khách Hàng 40', 'Địa chỉ 40', '0911111040'),
('Khách Hàng 41', 'Địa chỉ 41', '0911111041'), ('Khách Hàng 42', 'Địa chỉ 42', '0911111042'), ('Khách Hàng 43', 'Địa chỉ 43', '0911111043'), ('Khách Hàng 44', 'Địa chỉ 44', '0911111044'), ('Khách Hàng 45', 'Địa chỉ 45', '0911111045'),
('Khách Hàng 46', 'Địa chỉ 46', '0911111046'), ('Khách Hàng 47', 'Địa chỉ 47', '0911111047'), ('Khách Hàng 48', 'Địa chỉ 48', '0911111048'), ('Khách Hàng 49', 'Địa chỉ 49', '0911111049'), ('Khách Hàng 50', 'Địa chỉ 50', '0911111050'),
('Khách Hàng 51', 'Địa chỉ 51', '0911111051'), ('Khách Hàng 52', 'Địa chỉ 52', '0911111052'), ('Khách Hàng 53', 'Địa chỉ 53', '0911111053'), ('Khách Hàng 54', 'Địa chỉ 54', '0911111054'), ('Khách Hàng 55', 'Địa chỉ 55', '0911111055'),
('Khách Hàng 56', 'Địa chỉ 56', '0911111056'), ('Khách Hàng 57', 'Địa chỉ 57', '0911111057'), ('Khách Hàng 58', 'Địa chỉ 58', '0911111058'), ('Khách Hàng 59', 'Địa chỉ 59', '0911111059'), ('Khách Hàng 60', 'Địa chỉ 60', '0911111060'),
('Khách Hàng 61', 'Địa chỉ 61', '0911111061'), ('Khách Hàng 62', 'Địa chỉ 62', '0911111062'), ('Khách Hàng 63', 'Địa chỉ 63', '0911111063'), ('Khách Hàng 64', 'Địa chỉ 64', '0911111064'), ('Khách Hàng 65', 'Địa chỉ 65', '0911111065'),
('Khách Hàng 66', 'Địa chỉ 66', '0911111066'), ('Khách Hàng 67', 'Địa chỉ 67', '0911111067'), ('Khách Hàng 68', 'Địa chỉ 68', '0911111068'), ('Khách Hàng 69', 'Địa chỉ 69', '0911111069'), ('Khách Hàng 70', 'Địa chỉ 70', '0911111070'),
('Khách Hàng 71', 'Địa chỉ 71', '0911111071'), ('Khách Hàng 72', 'Địa chỉ 72', '0911111072'), ('Khách Hàng 73', 'Địa chỉ 73', '0911111073'), ('Khách Hàng 74', 'Địa chỉ 74', '0911111074'), ('Khách Hàng 75', 'Địa chỉ 75', '0911111075'),
('Khách Hàng 76', 'Địa chỉ 76', '0911111076'), ('Khách Hàng 77', 'Địa chỉ 77', '0911111077'), ('Khách Hàng 78', 'Địa chỉ 78', '0911111078'), ('Khách Hàng 79', 'Địa chỉ 79', '0911111079'), ('Khách Hàng 80', 'Địa chỉ 80', '0911111080'),
('Khách Hàng 81', 'Địa chỉ 81', '0911111081'), ('Khách Hàng 82', 'Địa chỉ 82', '0911111082'), ('Khách Hàng 83', 'Địa chỉ 83', '0911111083'), ('Khách Hàng 84', 'Địa chỉ 84', '0911111084'), ('Khách Hàng 85', 'Địa chỉ 85', '0911111085'),
('Khách Hàng 86', 'Địa chỉ 86', '0911111086'), ('Khách Hàng 87', 'Địa chỉ 87', '0911111087'), ('Khách Hàng 88', 'Địa chỉ 88', '0911111088'), ('Khách Hàng 89', 'Địa chỉ 89', '0911111089'), ('Khách Hàng 90', 'Địa chỉ 90', '0911111090'),
('Khách Hàng 91', 'Địa chỉ 91', '0911111091'), ('Khách Hàng 92', 'Địa chỉ 92', '0911111092'), ('Khách Hàng 93', 'Địa chỉ 93', '0911111093'), ('Khách Hàng 94', 'Địa chỉ 94', '0911111094'), ('Khách Hàng 95', 'Địa chỉ 95', '0911111095'),
('Khách Hàng 96', 'Địa chỉ 96', '0911111096'), ('Khách Hàng 97', 'Địa chỉ 97', '0911111097'), ('Khách Hàng 98', 'Địa chỉ 98', '0911111098'), ('Khách Hàng 99', 'Địa chỉ 99', '0911111099'), ('Khách Hàng 100', 'Địa chỉ 100', '0911111100');

-- 3.3. NHÂN VIÊN
INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address, sta_salary, sta_start_date, sta_username, sta_password, sta_role) VALUES
('Nguyễn Quản Lý', '1990-01-01', '0901000000', 'Hà Nội', 20000000, '2020-01-01', 'admin', '123', 'Admin'),
('Trần Văn A', '1995-02-15', '0901000001', 'Hà Nội', 8000000, '2023-05-10', 'user1', '123', 'Staff'),
('Lê Thị B', '1996-03-20', '0901000002', 'TP.HCM', 8500000, '2023-06-15', 'user2', '123', 'Staff'),
('Phạm Văn C', '1997-04-25', '0901000003', 'Đà Nẵng', 9000000, '2023-02-20', 'user3', '123', 'Staff'),
('Hoàng Thị D', '1998-05-30', '0901000004', 'Cần Thơ', 7500000, '2024-01-10', 'user4', '123', 'Staff'),
('Vũ Văn E', '1999-06-05', '0901000005', 'Hải Phòng', 8200000, '2023-11-05', 'user5', '123', 'Staff'),
('Đặng Thị F', '2000-07-10', '0901000006', 'Hà Nội', 7800000, '2024-03-01', 'user6', '123', 'Staff'),
('Bùi Văn G', '1991-08-15', '0901000007', 'TP.HCM', 12000000, '2021-08-15', 'user7', '123', 'Staff'),
('Đỗ Thị H', '1992-09-20', '0901000008', 'Đà Nẵng', 11500000, '2022-09-20', 'user8', '123', 'Staff'),
('Hồ Văn I', '1993-10-25', '0901000009', 'Nha Trang', 10000000, '2022-12-01', 'user9', '123', 'Staff'),
('Ngô Thị K', '1994-11-30', '0901000010', 'Huế', 9500000, '2023-04-30', 'user10', '123', 'Staff'),
('Dương Văn L', '1995-12-05', '0901000011', 'Vinh', 8800000, '2023-07-07', 'user11', '123', 'Staff'),
('Lý Thị M', '1996-01-10', '0901000012', 'Hà Nội', 8500000, '2023-09-12', 'user12', '123', 'Staff'),
('Trương Văn N', '1997-02-15', '0901000013', 'TP.HCM', 9200000, '2023-01-25', 'user13', '123', 'Staff'),
('Đinh Thị O', '1998-03-20', '0901000014', 'Cần Thơ', 7600000, '2024-02-14', 'user14', '123', 'Staff'),
('Lâm Văn P', '1999-04-25', '0901000015', 'Hải Dương', 8100000, '2023-10-20', 'user15', '123', 'Staff'),
('Mai Thị Q', '2000-05-30', '0901000016', 'Thái Bình', 7900000, '2024-01-05', 'user16', '123', 'Staff'),
('Cao Văn R', '1991-06-05', '0901000017', 'Nam Định', 13000000, '2021-05-05', 'user17', '123', 'Staff'),
('Phan Thị S', '1992-07-10', '0901000018', 'Ninh Bình', 12500000, '2022-03-10', 'user18', '123', 'Staff'),
('Hà Văn T', '1993-08-15', '0901000019', 'Thanh Hóa', 11000000, '2022-11-15', 'user19', '123', 'Staff');

INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address, sta_salary, sta_start_date, sta_username, sta_password, sta_role) VALUES
('Nhân Viên 21', '2000-01-01', '0900000021', 'TPHCM', 6500000, '2024-01-01', 'user21', '123', 'Staff'), ('Nhân Viên 22', '2000-01-01', '0900000022', 'TPHCM', 6500000, '2024-01-01', 'user22', '123', 'Staff'),
('Nhân Viên 23', '2000-01-01', '0900000023', 'TPHCM', 6500000, '2024-01-01', 'user23', '123', 'Staff'), ('Nhân Viên 24', '2000-01-01', '0900000024', 'TPHCM', 6500000, '2024-01-01', 'user24', '123', 'Staff'),
('Nhân Viên 25', '2000-01-01', '0900000025', 'TPHCM', 6500000, '2024-01-01', 'user25', '123', 'Staff'), ('Nhân Viên 26', '2000-01-01', '0900000026', 'TPHCM', 6500000, '2024-01-01', 'user26', '123', 'Staff'),
('Nhân Viên 27', '2000-01-01', '0900000027', 'TPHCM', 6500000, '2024-01-01', 'user27', '123', 'Staff'), ('Nhân Viên 28', '2000-01-01', '0900000028', 'TPHCM', 6500000, '2024-01-01', 'user28', '123', 'Staff'),
('Nhân Viên 29', '2000-01-01', '0900000029', 'TPHCM', 6500000, '2024-01-01', 'user29', '123', 'Staff'), ('Nhân Viên 30', '2000-01-01', '0900000030', 'TPHCM', 6500000, '2024-01-01', 'user30', '123', 'Staff'),
('Nhân Viên 31', '2000-01-01', '0900000031', 'TPHCM', 6500000, '2024-01-01', 'user31', '123', 'Staff'), ('Nhân Viên 32', '2000-01-01', '0900000032', 'TPHCM', 6500000, '2024-01-01', 'user32', '123', 'Staff'),
('Nhân Viên 33', '2000-01-01', '0900000033', 'TPHCM', 6500000, '2024-01-01', 'user33', '123', 'Staff'), ('Nhân Viên 34', '2000-01-01', '0900000034', 'TPHCM', 6500000, '2024-01-01', 'user34', '123', 'Staff'),
('Nhân Viên 35', '2000-01-01', '0900000035', 'TPHCM', 6500000, '2024-01-01', 'user35', '123', 'Staff'), ('Nhân Viên 36', '2000-01-01', '0900000036', 'TPHCM', 6500000, '2024-01-01', 'user36', '123', 'Staff'),
('Nhân Viên 37', '2000-01-01', '0900000037', 'TPHCM', 6500000, '2024-01-01', 'user37', '123', 'Staff'), ('Nhân Viên 38', '2000-01-01', '0900000038', 'TPHCM', 6500000, '2024-01-01', 'user38', '123', 'Staff'),
('Nhân Viên 39', '2000-01-01', '0900000039', 'TPHCM', 6500000, '2024-01-01', 'user39', '123', 'Staff'), ('Nhân Viên 40', '2000-01-01', '0900000040', 'TPHCM', 6500000, '2024-01-01', 'user40', '123', 'Staff'),
('Nhân Viên 41', '2000-01-01', '0900000041', 'TPHCM', 6500000, '2024-01-01', 'user41', '123', 'Staff'), ('Nhân Viên 42', '2000-01-01', '0900000042', 'TPHCM', 6500000, '2024-01-01', 'user42', '123', 'Staff'),
('Nhân Viên 43', '2000-01-01', '0900000043', 'TPHCM', 6500000, '2024-01-01', 'user43', '123', 'Staff'), ('Nhân Viên 44', '2000-01-01', '0900000044', 'TPHCM', 6500000, '2024-01-01', 'user44', '123', 'Staff'),
('Nhân Viên 45', '2000-01-01', '0900000045', 'TPHCM', 6500000, '2024-01-01', 'user45', '123', 'Staff'), ('Nhân Viên 46', '2000-01-01', '0900000046', 'TPHCM', 6500000, '2024-01-01', 'user46', '123', 'Staff'),
('Nhân Viên 47', '2000-01-01', '0900000047', 'TPHCM', 6500000, '2024-01-01', 'user47', '123', 'Staff'), ('Nhân Viên 48', '2000-01-01', '0900000048', 'TPHCM', 6500000, '2024-01-01', 'user48', '123', 'Staff'),
('Nhân Viên 49', '2000-01-01', '0900000049', 'TPHCM', 6500000, '2024-01-01', 'user49', '123', 'Staff'), ('Nhân Viên 50', '2000-01-01', '0900000050', 'TPHCM', 6500000, '2024-01-01', 'user50', '123', 'Staff'),
('Nhân Viên 51', '2000-01-01', '0900000051', 'TPHCM', 6500000, '2024-01-01', 'user51', '123', 'Staff'), ('Nhân Viên 52', '2000-01-01', '0900000052', 'TPHCM', 6500000, '2024-01-01', 'user52', '123', 'Staff'),
('Nhân Viên 53', '2000-01-01', '0900000053', 'TPHCM', 6500000, '2024-01-01', 'user53', '123', 'Staff'), ('Nhân Viên 54', '2000-01-01', '0900000054', 'TPHCM', 6500000, '2024-01-01', 'user54', '123', 'Staff'),
('Nhân Viên 55', '2000-01-01', '0900000055', 'TPHCM', 6500000, '2024-01-01', 'user55', '123', 'Staff'), ('Nhân Viên 56', '2000-01-01', '0900000056', 'TPHCM', 6500000, '2024-01-01', 'user56', '123', 'Staff'),
('Nhân Viên 57', '2000-01-01', '0900000057', 'TPHCM', 6500000, '2024-01-01', 'user57', '123', 'Staff'), ('Nhân Viên 58', '2000-01-01', '0900000058', 'TPHCM', 6500000, '2024-01-01', 'user58', '123', 'Staff'),
('Nhân Viên 59', '2000-01-01', '0900000059', 'TPHCM', 6500000, '2024-01-01', 'user59', '123', 'Staff'), ('Nhân Viên 60', '2000-01-01', '0900000060', 'TPHCM', 6500000, '2024-01-01', 'user60', '123', 'Staff'),
('Nhân Viên 61', '2000-01-01', '0900000061', 'TPHCM', 6500000, '2024-01-01', 'user61', '123', 'Staff'), ('Nhân Viên 62', '2000-01-01', '0900000062', 'TPHCM', 6500000, '2024-01-01', 'user62', '123', 'Staff'),
('Nhân Viên 63', '2000-01-01', '0900000063', 'TPHCM', 6500000, '2024-01-01', 'user63', '123', 'Staff'), ('Nhân Viên 64', '2000-01-01', '0900000064', 'TPHCM', 6500000, '2024-01-01', 'user64', '123', 'Staff'),
('Nhân Viên 65', '2000-01-01', '0900000065', 'TPHCM', 6500000, '2024-01-01', 'user65', '123', 'Staff'), ('Nhân Viên 66', '2000-01-01', '0900000066', 'TPHCM', 6500000, '2024-01-01', 'user66', '123', 'Staff'),
('Nhân Viên 67', '2000-01-01', '0900000067', 'TPHCM', 6500000, '2024-01-01', 'user67', '123', 'Staff'), ('Nhân Viên 68', '2000-01-01', '0900000068', 'TPHCM', 6500000, '2024-01-01', 'user68', '123', 'Staff'),
('Nhân Viên 69', '2000-01-01', '0900000069', 'TPHCM', 6500000, '2024-01-01', 'user69', '123', 'Staff'), ('Nhân Viên 70', '2000-01-01', '0900000070', 'TPHCM', 6500000, '2024-01-01', 'user70', '123', 'Staff'),
('Nhân Viên 71', '2000-01-01', '0900000071', 'TPHCM', 6500000, '2024-01-01', 'user71', '123', 'Staff'), ('Nhân Viên 72', '2000-01-01', '0900000072', 'TPHCM', 6500000, '2024-01-01', 'user72', '123', 'Staff'),
('Nhân Viên 73', '2000-01-01', '0900000073', 'TPHCM', 6500000, '2024-01-01', 'user73', '123', 'Staff'), ('Nhân Viên 74', '2000-01-01', '0900000074', 'TPHCM', 6500000, '2024-01-01', 'user74', '123', 'Staff'),
('Nhân Viên 75', '2000-01-01', '0900000075', 'TPHCM', 6500000, '2024-01-01', 'user75', '123', 'Staff'), ('Nhân Viên 76', '2000-01-01', '0900000076', 'TPHCM', 6500000, '2024-01-01', 'user76', '123', 'Staff'),
('Nhân Viên 77', '2000-01-01', '0900000077', 'TPHCM', 6500000, '2024-01-01', 'user77', '123', 'Staff'), ('Nhân Viên 78', '2000-01-01', '0900000078', 'TPHCM', 6500000, '2024-01-01', 'user78', '123', 'Staff'),
('Nhân Viên 79', '2000-01-01', '0900000079', 'TPHCM', 6500000, '2024-01-01', 'user79', '123', 'Staff'), ('Nhân Viên 80', '2000-01-01', '0900000080', 'TPHCM', 6500000, '2024-01-01', 'user80', '123', 'Staff'),
('Nhân Viên 81', '2000-01-01', '0900000081', 'TPHCM', 6500000, '2024-01-01', 'user81', '123', 'Staff'), ('Nhân Viên 82', '2000-01-01', '0900000082', 'TPHCM', 6500000, '2024-01-01', 'user82', '123', 'Staff'),
('Nhân Viên 83', '2000-01-01', '0900000083', 'TPHCM', 6500000, '2024-01-01', 'user83', '123', 'Staff'), ('Nhân Viên 84', '2000-01-01', '0900000084', 'TPHCM', 6500000, '2024-01-01', 'user84', '123', 'Staff'),
('Nhân Viên 85', '2000-01-01', '0900000085', 'TPHCM', 6500000, '2024-01-01', 'user85', '123', 'Staff'), ('Nhân Viên 86', '2000-01-01', '0900000086', 'TPHCM', 6500000, '2024-01-01', 'user86', '123', 'Staff'),
('Nhân Viên 87', '2000-01-01', '0900000087', 'TPHCM', 6500000, '2024-01-01', 'user87', '123', 'Staff'), ('Nhân Viên 88', '2000-01-01', '0900000088', 'TPHCM', 6500000, '2024-01-01', 'user88', '123', 'Staff'),
('Nhân Viên 89', '2000-01-01', '0900000089', 'TPHCM', 6500000, '2024-01-01', 'user89', '123', 'Staff'), ('Nhân Viên 90', '2000-01-01', '0900000090', 'TPHCM', 6500000, '2024-01-01', 'user90', '123', 'Staff'),
('Nhân Viên 91', '2000-01-01', '0900000091', 'TPHCM', 6500000, '2024-01-01', 'user91', '123', 'Staff'), ('Nhân Viên 92', '2000-01-01', '0900000092', 'TPHCM', 6500000, '2024-01-01', 'user92', '123', 'Staff'),
('Nhân Viên 93', '2000-01-01', '0900000093', 'TPHCM', 6500000, '2024-01-01', 'user93', '123', 'Staff'), ('Nhân Viên 94', '2000-01-01', '0900000094', 'TPHCM', 6500000, '2024-01-01', 'user94', '123', 'Staff'),
('Nhân Viên 95', '2000-01-01', '0900000095', 'TPHCM', 6500000, '2024-01-01', 'user95', '123', 'Staff'), ('Nhân Viên 96', '2000-01-01', '0900000096', 'TPHCM', 6500000, '2024-01-01', 'user96', '123', 'Staff'),
('Nhân Viên 97', '2000-01-01', '0900000097', 'TPHCM', 6500000, '2024-01-01', 'user97', '123', 'Staff'), ('Nhân Viên 98', '2000-01-01', '0900000098', 'TPHCM', 6500000, '2024-01-01', 'user98', '123', 'Staff'),
('Nhân Viên 99', '2000-01-01', '0900000099', 'TPHCM', 6500000, '2024-01-01', 'user99', '123', 'Staff'), ('Nhân Viên 100', '2000-01-01', '0900000100', 'TPHCM', 6500000, '2024-01-01', 'user100', '123', 'Staff');

-- 3.4. LOẠI SẢN PHẨM
INSERT INTO ProductTypes (type_name) VALUES
('Laptop Văn Phòng'), ('Laptop Gaming'), ('Macbook'), ('Điện thoại iPhone'), ('Điện thoại Android'), ('Máy tính bảng'), ('Đồng hồ thông minh'), ('Tai nghe'), ('Loa Bluetooth'), ('Bàn phím'),
('Chuột máy tính'), ('Màn hình'), ('Ram - Bộ nhớ'), ('Ổ cứng SSD'), ('VGA - Card màn hình'), ('Mainboard'), ('Case - Vỏ máy'), ('Nguồn máy tính'), ('Phần mềm'), ('Camera an ninh');

-- 3.5. SẢN PHẨM (Dữ liệu thật)
INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID) VALUES
('Laptop Dell XPS 13 Plus', 45000000, 10, 1, 1), ('Laptop Dell Inspiron 15', 15000000, 20, 1, 1), ('Laptop Asus Zenbook 14', 25000000, 15, 1, 2), ('Laptop Asus TUF Gaming', 22000000, 12, 2, 2), ('Laptop HP Pavilion', 18000000, 18, 1, 5),
('Laptop Lenovo ThinkPad X1', 35000000, 8, 1, 6), ('Laptop MSI Raider GE78', 55000000, 5, 2, 9), ('MacBook Air M2', 28000000, 25, 3, 4), ('MacBook Pro M3 Max', 60000000, 5, 3, 4), ('iPhone 15 Pro Max', 33000000, 30, 4, 4),
('iPhone 14 Plus', 20000000, 20, 4, 4), ('Samsung Galaxy S24 Ultra', 30000000, 22, 5, 3), ('Samsung Galaxy Z Fold 5', 35000000, 10, 5, 3), ('Xiaomi 14 Ultra', 25000000, 15, 5, 20), ('iPad Pro M2 11 inch', 20000000, 15, 6, 4),
('Samsung Galaxy Tab S9', 18000000, 12, 6, 3), ('Apple Watch Series 9', 10000000, 25, 7, 4), ('Samsung Galaxy Watch 6', 7000000, 30, 7, 3), ('Tai nghe AirPods Pro 2', 5500000, 50, 8, 4), ('Tai nghe Sony WH-1000XM5', 7500000, 20, 8, 7),
('Loa Marshall Stanmore III', 9000000, 10, 9, 20), ('Loa JBL Charge 5', 3500000, 25, 9, 20), ('Bàn phím cơ Keychron K2', 1800000, 30, 10, 20), ('Bàn phím Logitech MX Keys', 2500000, 20, 10, 12), ('Chuột Logitech MX Master 3S', 2200000, 40, 11, 12),
('Chuột Gaming Logitech G502', 1000000, 35, 11, 12), ('Màn hình Dell UltraSharp U2422H', 6000000, 15, 12, 1), ('Màn hình LG 27UP850 4K', 9000000, 10, 12, 8), ('RAM Kingston Fury 16GB', 1200000, 50, 13, 11), ('RAM Corsair Vengeance 32GB', 2500000, 30, 13, 19),
('SSD Samsung 980 Pro 1TB', 2800000, 40, 14, 3), ('SSD Western Digital Black 500GB', 1500000, 45, 14, 15), ('VGA RTX 4090 Gaming OC', 50000000, 3, 15, 10), ('VGA GTX 1660 Super', 5000000, 20, 15, 10), ('Mainboard Asus ROG Strix Z790', 9000000, 8, 16, 2),
('CPU Intel Core i9 14900K', 15000000, 10, 15, 13), ('CPU AMD Ryzen 9 7950X', 14000000, 10, 15, 14), ('Nguồn Corsair RM850x', 3000000, 20, 18, 19), ('Vỏ case NZXT H9 Flow', 4000000, 15, 17, 19), ('Camera Wifi Imou Ranger 2', 600000, 60, 20, 20);

-- Chèn thêm các sản phẩm ảo để đủ 100
INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID) VALUES
('Sản Phẩm 41', 4100000, 100, 1, 1), ('Sản Phẩm 42', 4200000, 100, 2, 2), ('Sản Phẩm 43', 4300000, 100, 3, 3), ('Sản Phẩm 44', 4400000, 100, 4, 4), ('Sản Phẩm 45', 4500000, 100, 5, 5),
('Sản Phẩm 46', 4600000, 100, 6, 6), ('Sản Phẩm 47', 4700000, 100, 7, 7), ('Sản Phẩm 48', 4800000, 100, 8, 8), ('Sản Phẩm 49', 4900000, 100, 9, 9), ('Sản Phẩm 50', 5000000, 100, 10, 10),
('Sản Phẩm 51', 5100000, 100, 11, 11), ('Sản Phẩm 52', 5200000, 100, 12, 12), ('Sản Phẩm 53', 5300000, 100, 13, 13), ('Sản Phẩm 54', 5400000, 100, 14, 14), ('Sản Phẩm 55', 5500000, 100, 15, 15),
('Sản Phẩm 56', 5600000, 100, 16, 16), ('Sản Phẩm 57', 5700000, 100, 17, 17), ('Sản Phẩm 58', 5800000, 100, 18, 18), ('Sản Phẩm 59', 5900000, 100, 19, 19), ('Sản Phẩm 60', 6000000, 100, 20, 20),
('Sản Phẩm 61', 6100000, 100, 1, 1), ('Sản Phẩm 62', 6200000, 100, 2, 2), ('Sản Phẩm 63', 6300000, 100, 3, 3), ('Sản Phẩm 64', 6400000, 100, 4, 4), ('Sản Phẩm 65', 6500000, 100, 5, 5),
('Sản Phẩm 66', 6600000, 100, 6, 6), ('Sản Phẩm 67', 6700000, 100, 7, 7), ('Sản Phẩm 68', 6800000, 100, 8, 8), ('Sản Phẩm 69', 6900000, 100, 9, 9), ('Sản Phẩm 70', 7000000, 100, 10, 10),
('Sản Phẩm 71', 7100000, 100, 11, 11), ('Sản Phẩm 72', 7200000, 100, 12, 12), ('Sản Phẩm 73', 7300000, 100, 13, 13), ('Sản Phẩm 74', 7400000, 100, 14, 14), ('Sản Phẩm 75', 7500000, 100, 15, 15),
('Sản Phẩm 76', 7600000, 100, 16, 16), ('Sản Phẩm 77', 7700000, 100, 17, 17), ('Sản Phẩm 78', 7800000, 100, 18, 18), ('Sản Phẩm 79', 7900000, 100, 19, 19), ('Sản Phẩm 80', 8000000, 100, 20, 20),
('Sản Phẩm 81', 8100000, 100, 1, 1), ('Sản Phẩm 82', 8200000, 100, 2, 2), ('Sản Phẩm 83', 8300000, 100, 3, 3), ('Sản Phẩm 84', 8400000, 100, 4, 4), ('Sản Phẩm 85', 8500000, 100, 5, 5),
('Sản Phẩm 86', 8600000, 100, 6, 6), ('Sản Phẩm 87', 8700000, 100, 7, 7), ('Sản Phẩm 88', 8800000, 100, 8, 8), ('Sản Phẩm 89', 8900000, 100, 9, 9), ('Sản Phẩm 90', 9000000, 100, 10, 10),
('Sản Phẩm 91', 9100000, 100, 11, 11), ('Sản Phẩm 92', 9200000, 100, 12, 12), ('Sản Phẩm 93', 9300000, 100, 13, 13), ('Sản Phẩm 94', 9400000, 100, 14, 14), ('Sản Phẩm 95', 9500000, 100, 15, 15),
('Sản Phẩm 96', 9600000, 100, 16, 16), ('Sản Phẩm 97', 9700000, 100, 17, 17), ('Sản Phẩm 98', 9800000, 100, 18, 18), ('Sản Phẩm 99', 9900000, 100, 19, 19), ('Sản Phẩm 100', 10000000, 100, 20, 20);

-- 3.6. HÓA ĐƠN & CHI TIẾT
-- (Dữ liệu Hóa đơn như trên, lưu ý hàm DATE_SUB đổi thành DATE(..., '-x days'))
-- Hóa đơn 1: Mua ngay bây giờ
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (1, 1, 1, 5000000, DATETIME('now'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (1, 50, 1, 5000000); -- Sản phẩm 50 giá 5tr

-- Hóa đơn 2: Mua cách đây 2 tiếng
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (2, 2, 5, 10000000, DATETIME('now', '-2 hours'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (2, 50, 2, 5000000);

-- Hóa đơn 3: Mua cách đây 5 tiếng
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (3, 3, 8, 4500000, DATETIME('now', '-5 hours'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (3, 45, 1, 4500000); -- SP 45 giá 4.5tr

-- Hóa đơn 4: Hôm qua
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (4, 1, 10, 8200000, DATETIME('now', '-1 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (4, 41, 2, 4100000); -- SP 41

-- Hóa đơn 5: Cách đây 3 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (5, 2, 12, 15000000, DATETIME('now', '-3 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (5, 1, 1, 15000000); -- Laptop Dell (ID 1)

-- Hóa đơn 6: Cách đây 5 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (6, 4, 15, 6000000, DATETIME('now', '-5 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (6, 60, 1, 6000000);

-- Hóa đơn 7: Cách đây 6 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (7, 5, 20, 9000000, DATETIME('now', '-6 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (7, 45, 2, 4500000);

-- Hóa đơn 8: Cách đây 10 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (8, 1, 22, 20000000, DATETIME('now', '-10 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (8, 50, 4, 5000000);

-- Hóa đơn 9: Cách đây 15 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (9, 2, 25, 30000000, DATETIME('now', '-15 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (9, 12, 1, 30000000); -- Samsung S24 (ID 12)

-- Hóa đơn 10: Cách đây 20 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (10, 3, 30, 5500000, DATETIME('now', '-20 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (10, 55, 1, 5500000);

-- Hóa đơn 11: Cách đây 28 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (11, 4, 35, 12000000, DATETIME('now', '-28 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (11, 60, 2, 6000000);

-- Hóa đơn 12: Cách đây 1 tháng 5 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (12, 1, 40, 45000000, DATETIME('now', '-1 months', '-5 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (12, 1, 1, 45000000); -- Dell XPS (ID 1)

-- Hóa đơn 13: Cách đây 2 tháng
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (13, 2, 45, 8400000, DATETIME('now', '-2 months'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (13, 42, 2, 4200000);

-- Hóa đơn 14: Cách đây 2 tháng 15 ngày
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (14, 5, 50, 25000000, DATETIME('now', '-2 months', '-15 days'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (14, 14, 1, 25000000); -- Xiaomi (ID 14)

-- Hóa đơn 15: Cách đây 4 tháng
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (15, 1, 55, 10000000, DATETIME('now', '-4 months'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (15, 50, 2, 5000000);

-- Hóa đơn 16: Cách đây 5 tháng
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (16, 2, 60, 60000000, DATETIME('now', '-5 months'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (16, 9, 1, 60000000); -- Macbook Pro (ID 9)

-- Hóa đơn 17: Cách đây 7 tháng
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (17, 3, 65, 5500000, DATETIME('now', '-7 months'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (17, 55, 1, 5500000);

-- Hóa đơn 18: Cách đây 9 tháng (Đầu năm)
INSERT INTO Invoices (inv_ID, sta_ID, cus_ID, inv_price, inv_date)
VALUES (18, 4, 70, 18000000, DATETIME('now', '-9 months'));
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price)
VALUES (18, 5, 1, 18000000); -- HP Pavilion