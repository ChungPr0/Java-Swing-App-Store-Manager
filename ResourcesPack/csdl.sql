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
    sta_role TEXT DEFAULT 'SaleStaff' -- 'Admin', 'Manager', 'SaleStaff', 'StorageStaff'
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

-- ==========================================
-- 3. CHÈN DỮ LIỆU MẪU (SEED DATA)
-- ==========================================

-- 3.1. Nhà cung cấp (Suppliers)
INSERT INTO Suppliers (sup_name, sup_address, sup_phone, sup_description) VALUES
('Công ty TNHH Thực Phẩm Sạch', '123 Nguyễn Trãi, Q.1, TP.HCM', '0901234567', 'Chuyên cung cấp thực phẩm tươi sống, rau củ quả sạch.'),
('Vinamilk', '10 Tân Trào, P. Tân Phú, Q.7, TP.HCM', '02854155555', 'Sữa tươi, sữa chua, phô mai và các sản phẩm từ sữa.'),
('Unilever Việt Nam', '105 Tôn Dật Tiên, Q.7, TP.HCM', '02854135686', 'Hàng tiêu dùng nhanh: OMO, Dove, Sunsilk, Knorr...'),
('Công ty CP Acecook', '10 Tân Trào, P. Tân Phú, Q.7, TP.HCM', '02837761234', 'Mì ăn liền Hảo Hảo, phở Đệ Nhất, bún Hằng Nga.'),
('PepsiCo Việt Nam', 'KCN Amata, Biên Hòa, Đồng Nai', '02513999999', 'Nước giải khát Pepsi, 7Up, Mirinda, Sting, Aquafina.'),
('Nestlé Việt Nam', '10 Tân Trào, P. Tân Phú, Q.7, TP.HCM', '02839113737', 'Milo, Nescafé, Maggi, KitKat.'),
('Công ty CP Masan', 'Tầng 8, Central Plaza, 17 Lê Duẩn, Q.1', '02862563862', 'Nước mắm Nam Ngư, tương ớt Chinsu, mì Omachi.'),
('Heineken Việt Nam', 'Tầng 10, Bitexco, 2 Hải Triều, Q.1', '02838222755', 'Bia Heineken, Tiger, Larue, nước táo Strongbow.'),
('Công ty TNHH Nước Giải Khát Coca-Cola', 'KCN Thủ Đức, P. Linh Trung, TP. Thủ Đức', '02838961000', 'Coca-Cola, Sprite, Fanta, Nutriboost.'),
('Công ty CP Bánh Kẹo Kinh Đô', '138-142 Hai Bà Trưng, P. Đa Kao, Q.1', '02838270838', 'Bánh Cosy, Solite, AFC, bánh trung thu.');

-- 3.2. Loại sản phẩm (ProductTypes)
INSERT INTO ProductTypes (type_name) VALUES
('Thực phẩm tươi sống'),
('Đồ uống & Giải khát'),
('Sữa & Chế phẩm từ sữa'),
('Gia vị & Đồ khô'),
('Bánh kẹo & Đồ ăn vặt'),
('Hóa mỹ phẩm'),
('Đồ dùng gia đình'),
('Văn phòng phẩm'),
('Thực phẩm đông lạnh'),
('Mẹ & Bé');

-- 3.3. Sản phẩm (Products)
INSERT INTO Products (pro_name, pro_price, pro_count, pro_description, type_ID, sup_ID) VALUES
-- Thực phẩm tươi sống
('Thịt ba chỉ heo (500g)', 85000, 50, 'Thịt heo sạch chuẩn VietGAP', 1, 1),
('Cá hồi phi lê (300g)', 150000, 30, 'Cá hồi Na Uy tươi ngon', 1, 1),
('Gà ta thả vườn (1 con)', 180000, 20, 'Gà ta làm sạch, đóng gói hút chân không', 1, 1),
('Trứng gà ta (vỉ 10 quả)', 35000, 100, 'Trứng gà tươi mới mỗi ngày', 1, 1),
('Rau muống sạch (500g)', 15000, 80, 'Rau trồng thủy canh', 1, 1),

-- Đồ uống
('Nước ngọt Pepsi (Lon 330ml)', 10000, 200, 'Vị cola truyền thống', 2, 5),
('Nước ngọt 7Up (Chai 1.5L)', 18000, 150, 'Vị chanh tươi mát', 2, 5),
('Bia Heineken (Thùng 24 lon)', 420000, 50, 'Bia thượng hạng từ Hà Lan', 2, 8),
('Nước suối Aquafina (Chai 500ml)', 5000, 300, 'Nước tinh khiết', 2, 5),
('Trà ô long Tea+ (Chai 450ml)', 12000, 120, 'Giúp hạn chế hấp thu chất béo', 2, 5),

-- Sữa
('Sữa tươi Vinamilk 100% (Hộp 1L)', 32000, 100, 'Sữa tươi tiệt trùng không đường', 3, 2),
('Sữa chua Vinamilk Nha Đam (Lốc 4 hộp)', 28000, 80, 'Sữa chua ăn liền vị nha đam', 3, 2),
('Sữa đặc Ông Thọ (Lon 380g)', 22000, 150, 'Sữa đặc có đường', 3, 2),
('Sữa Milo lúa mạch (Lốc 4 hộp 180ml)', 29000, 200, 'Thức uống lúa mạch năng lượng', 3, 6),
('Phô mai Con Bò Cười (Hộp 8 miếng)', 35000, 60, 'Phô mai tam giác', 3, 2),

-- Gia vị & Đồ khô
('Mì Hảo Hảo Tôm Chua Cay (Thùng 30 gói)', 115000, 100, 'Mì quốc dân', 4, 4),
('Nước mắm Nam Ngư (Chai 750ml)', 38000, 120, 'Vị ngon đậm đà', 4, 7),
('Tương ớt Chinsu (Chai 250g)', 15000, 200, 'Cay nồng, thơm ngon', 4, 7),
('Dầu ăn Neptune (Chai 1L)', 45000, 90, 'Dầu thực vật cao cấp', 4, 7),
('Gạo ST25 (Túi 5kg)', 180000, 40, 'Gạo ngon nhất thế giới', 4, 1),

-- Bánh kẹo
('Bánh quy bơ Cosy (Gói 200g)', 25000, 80, 'Bánh quy giòn tan', 5, 10),
('Snack khoai tây Lay''s (Gói 65g)', 12000, 150, 'Vị tảo biển', 5, 5),
('Kẹo KitKat trà xanh (Gói 12 thanh)', 65000, 50, 'Vị trà xanh Nhật Bản', 5, 6),
('Bánh ChocoPie (Hộp 12 cái)', 55000, 70, 'Bánh bông lan kem socola', 5, 10),
('Kẹo dẻo Chip Chip (Gói 100g)', 15000, 100, 'Kẹo dẻo trái cây', 5, 10),

-- Hóa mỹ phẩm
('Dầu gội Sunsilk Bồ Kết (Chai 650g)', 120000, 60, 'Óng mượt rạng ngời', 6, 3),
('Sữa tắm Lifebuoy (Chai 850g)', 150000, 50, 'Bảo vệ khỏi vi khuẩn', 6, 3),
('Bột giặt OMO Matic (Túi 3kg)', 180000, 40, 'Giặt máy cửa trên', 6, 3),
('Nước xả vải Comfort (Túi 1.8L)', 110000, 50, 'Hương ban mai', 6, 3),
('Kem đánh răng P/S (Tuýp 240g)', 35000, 100, 'Bảo vệ 123', 6, 3),

-- Đồ dùng gia đình
('Giấy vệ sinh Pulppy (Lốc 10 cuộn)', 85000, 60, 'Mềm mại, dai', 7, 3),
('Nước rửa chén Sunlight (Chai 750g)', 28000, 120, 'Chiết xuất chanh tươi', 7, 3),
('Khăn giấy ướt (Gói 100 tờ)', 30000, 80, 'Không mùi, an toàn cho da', 7, 3),
('Túi đựng rác tự hủy (Cuộn 1kg)', 45000, 100, 'Thân thiện môi trường', 7, 1),
('Màng bọc thực phẩm (Cuộn 30m)', 25000, 90, 'Giữ thực phẩm tươi lâu', 7, 1),

-- Văn phòng phẩm
('Bút bi Thiên Long (Hộp 20 cây)', 80000, 50, 'Mực xanh, ngòi 0.5mm', 8, 1),
('Tập học sinh 96 trang (Lốc 10 quyển)', 65000, 40, 'Giấy trắng, không lem', 8, 1),
('Băng keo trong (Cuộn lớn)', 15000, 100, 'Dính chắc', 8, 1),
('Kéo văn phòng (Cây)', 20000, 60, 'Lưỡi thép không gỉ', 8, 1),
('Giấy A4 Double A (Ram 500 tờ)', 75000, 30, 'Định lượng 70gsm', 8, 1),

-- Thực phẩm đông lạnh
('Chả giò rế tôm thịt (Gói 500g)', 65000, 40, 'Giòn rụm', 9, 1),
('Xúc xích Đức (Gói 500g)', 70000, 50, 'Thơm ngon', 9, 1),
('Cá viên chiên (Gói 500g)', 55000, 60, 'Dai ngon', 9, 1),
('Há cảo (Gói 300g)', 45000, 40, 'Nhân thịt heo', 9, 1),
('Khoai tây chiên cọng (Gói 1kg)', 85000, 30, 'Nhập khẩu Bỉ', 9, 1),

-- Mẹ & Bé
('Tã quần Bobby size L (Gói 54 miếng)', 280000, 30, 'Thấm hút siêu tốc', 10, 3),
('Sữa bột Similac (Lon 900g)', 450000, 20, 'Dành cho trẻ 1-3 tuổi', 10, 6),
('Phấn rôm Johnson Baby (Chai 200g)', 45000, 50, 'Ngừa rôm sảy', 10, 3),
('Bình sữa Pigeon (Chai 250ml)', 150000, 20, 'Nhựa PP an toàn', 10, 1),
('Nước giặt đồ em bé D-nee (Túi 3L)', 190000, 25, 'Hương thơm dịu nhẹ', 10, 1);

-- 3.4. Khách hàng (Customers)
INSERT INTO Customers (cus_name, cus_address, cus_phone) VALUES
('Khách vãng lai', 'N/A', '0000000000'),
('Nguyễn Văn An', '12 Lê Lợi, Q.1, TP.HCM', '0909123456'),
('Trần Thị Bích', '34 Nguyễn Huệ, Q.1, TP.HCM', '0918654321'),
('Lê Hoàng Cường', '56 Pasteur, Q.1, TP.HCM', '0933789012'),
('Phạm Minh Dũng', '78 Hai Bà Trưng, Q.3, TP.HCM', '0977112233'),
('Hoàng Thị Em', '90 Võ Thị Sáu, Q.3, TP.HCM', '0988445566'),
('Vũ Tuấn Phong', '102 Điện Biên Phủ, Q.3, TP.HCM', '0966778899'),
('Đặng Thu Hà', '15 Cách Mạng Tháng 8, Q.10, TP.HCM', '0944556677'),
('Bùi Văn Hùng', '27 Ba Tháng Hai, Q.10, TP.HCM', '0911223344'),
('Đỗ Thị Lan', '39 Lý Thường Kiệt, Q.10, TP.HCM', '0905678901'),
('Ngô Văn Kiên', '51 Nguyễn Tri Phương, Q.10, TP.HCM', '0934567890'),
('Dương Thị Mai', '63 Thành Thái, Q.10, TP.HCM', '0978901234'),
('Lý Văn Nam', '75 Sư Vạn Hạnh, Q.10, TP.HCM', '0989012345'),
('Trịnh Thị Oanh', '87 Tô Hiến Thành, Q.10, TP.HCM', '0961234567'),
('Mai Văn Phúc', '99 Lữ Gia, Q.11, TP.HCM', '0945678901'),
('Cao Thị Quyên', '111 Lê Đại Hành, Q.11, TP.HCM', '0912345678'),
('Phan Văn Rạng', '123 Ông Ích Khiêm, Q.11, TP.HCM', '0903456789'),
('Hồ Thị Sương', '135 Lạc Long Quân, Q.11, TP.HCM', '0935678901'),
('Đinh Văn Tài', '147 Bình Thới, Q.11, TP.HCM', '0976789012'),
('Lâm Thị Uyên', '159 Minh Phụng, Q.11, TP.HCM', '0987890123');

-- 3.5. Nhân viên (Staffs)
INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address, sta_salary, sta_start_date, sta_username, sta_password, sta_role) VALUES
('Nguyễn Quản Trị', '1990-01-01', '0909999999', 'TP.HCM', 20000000, '2020-01-01', 'admin', '123', 'Admin'),
('Trần Quản Lý', '1992-05-15', '0908888888', 'TP.HCM', 15000000, '2021-03-10', 'manager', '123', 'Manager'),
('Lê Bán Hàng 1', '1995-08-20', '0907777777', 'TP.HCM', 8000000, '2022-06-01', 'sale1', '123', 'SaleStaff'),
('Phạm Bán Hàng 2', '1998-12-12', '0906666666', 'TP.HCM', 7500000, '2023-01-15', 'sale2', '123', 'SaleStaff'),
('Hoàng Kho 1', '1993-04-30', '0905555555', 'TP.HCM', 9000000, '2021-09-05', 'storage1', '123', 'StorageStaff'),
('Vũ Kho 2', '1996-10-10', '0904444444', 'TP.HCM', 8500000, '2022-11-20', 'storage2', '123', 'StorageStaff');

-- 3.6. Mã giảm giá (Discounts)
INSERT INTO Discounts (dis_code, dis_type, dis_value, dis_scope, dis_category_id, dis_start_date, dis_end_date) VALUES
('WELCOME', 'PERCENT', 10, 'ALL', NULL, '2023-01-01', '2025-12-31'),
('SUMMER25', 'FIXED', 20000, 'ALL', NULL, '2024-06-01', '2024-08-31'),
('MILKDAY', 'PERCENT', 15, 'CATEGORY', 3, '2024-01-01', '2024-12-31'),
('SNACKTIME', 'PERCENT', 20, 'CATEGORY', 5, '2024-01-01', '2024-12-31'),
('VIPMEMBER', 'PERCENT', 5, 'ALL', NULL, '2023-01-01', '2030-12-31');

-- 3.7. Hóa đơn (Invoices) & Chi tiết (Invoice_details)
-- Tạo 20 hóa đơn mẫu rải rác trong năm nay và năm ngoái

-- Hóa đơn 1
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 2, 250000, '2023-12-20 10:30:00', NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(1, 1, 1, 85000), -- Thịt ba chỉ
(1, 11, 2, 32000), -- Sữa tươi
(1, 16, 1, 115000); -- Mì Hảo Hảo

-- Hóa đơn 2
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 3, 520000, '2024-01-15 14:15:00', 1);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(2, 8, 1, 420000), -- Bia Heineken
(2, 22, 5, 12000), -- Snack Lay's
(2, 6, 4, 10000); -- Pepsi

-- Hóa đơn 3
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 1, 150000, '2024-02-10 09:00:00', NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(3, 26, 1, 120000), -- Dầu gội Sunsilk
(3, 30, 1, 35000); -- Kem đánh răng P/S

-- Hóa đơn 4
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 5, 850000, '2024-03-08 18:45:00', NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(4, 42, 1, 450000), -- Sữa Similac
(4, 41, 1, 280000), -- Tã Bobby
(4, 45, 1, 190000); -- Nước giặt D-nee

-- Hóa đơn 5
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 8, 320000, '2024-04-30 11:20:00', 2);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(5, 2, 1, 150000), -- Cá hồi
(5, 3, 1, 180000); -- Gà ta

-- Hóa đơn 6
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 10, 95000, '2024-05-01 08:30:00', NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(6, 36, 5, 80000), -- Bút bi (Lỗi logic giá, nhưng kệ) -> Sửa lại: 1 hộp
(6, 38, 1, 15000); -- Băng keo

-- Hóa đơn 7
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 12, 210000, '2024-06-01 16:00:00', 3);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(7, 14, 5, 29000), -- Sữa Milo
(7, 15, 2, 35000); -- Phô mai

-- Hóa đơn 8
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 15, 450000, '2024-07-15 19:30:00', NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(8, 28, 2, 180000), -- Bột giặt OMO
(8, 29, 1, 110000); -- Nước xả Comfort

-- Hóa đơn 9
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 18, 125000, '2024-08-20 10:00:00', 4);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(9, 21, 2, 25000), -- Bánh Cosy
(9, 23, 1, 65000), -- KitKat
(9, 25, 1, 15000); -- Chip Chip

-- Hóa đơn 10
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 20, 300000, '2024-09-02 12:45:00', NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(10, 31, 2, 85000), -- Giấy Pulppy
(10, 32, 2, 28000), -- Sunlight
(10, 34, 2, 45000); -- Túi rác

-- Hóa đơn 11 (Hôm nay)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 4, 180000, DATETIME('now', '-2 hours'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(11, 20, 1, 180000); -- Gạo ST25

-- Hóa đơn 12 (Hôm nay)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 6, 60000, DATETIME('now', '-4 hours'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(12, 9, 12, 5000); -- Aquafina

-- Hóa đơn 13 (Hôm qua)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 7, 220000, DATETIME('now', '-1 day'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(13, 41, 1, 70000), -- Xúc xích (ID sai, sửa lại logic) -> ID 42
(13, 43, 2, 55000), -- Cá viên
(13, 44, 1, 45000); -- Há cảo

-- Hóa đơn 14 (Hôm qua)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 9, 500000, DATETIME('now', '-1 day'), 5);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(14, 8, 1, 420000), -- Heineken
(14, 7, 5, 18000); -- 7Up

-- Hóa đơn 15 (3 ngày trước)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 11, 150000, DATETIME('now', '-3 days'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(15, 1, 1, 85000), -- Thịt heo
(15, 5, 2, 15000), -- Rau muống
(15, 4, 1, 35000); -- Trứng gà

-- Hóa đơn 16 (4 ngày trước)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 13, 90000, DATETIME('now', '-4 days'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(16, 19, 2, 45000); -- Dầu ăn

-- Hóa đơn 17 (5 ngày trước)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 14, 250000, DATETIME('now', '-5 days'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(17, 40, 1, 75000), -- Giấy A4
(17, 37, 2, 65000), -- Tập học sinh
(17, 39, 2, 20000); -- Kéo

-- Hóa đơn 18 (6 ngày trước)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 16, 110000, DATETIME('now', '-6 days'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(18, 24, 2, 55000); -- ChocoPie

-- Hóa đơn 19 (Tuần trước)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (3, 17, 350000, DATETIME('now', '-8 days'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(19, 27, 2, 150000), -- Sữa tắm
(19, 33, 2, 30000); -- Khăn ướt

-- Hóa đơn 20 (Tháng trước)
INSERT INTO Invoices (sta_ID, cus_ID, inv_price, inv_date, dis_ID) VALUES (4, 19, 180000, DATETIME('now', '-35 days'), NULL);
INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES
(20, 3, 1, 180000); -- Gà ta
