USE HotelManagementDB_LocalSimple;
GO

-- Buoc 4: Du lieu khoi dau. Chay mot lan, DatPhong van de trong.
INSERT INTO Tang (MaTang, TenTang)
VALUES ('T1', N'Tầng 1'), ('T2', N'Tầng 2'), ('T3', N'Tầng 3');

INSERT INTO LoaiPhong (MaLoaiPhong, TenLoai, GiaTheoNgay, SoNguoiToiDa)
VALUES ('STD', N'Standard', 650000, 2),
       ('DLX', N'Deluxe', 950000, 3),
       ('STE', N'Suite', 1600000, 4);

INSERT INTO Phong (MaPhong, SoPhong, MaLoaiPhong, MaTang)
VALUES ('P101', N'101', 'STD', 'T1'), ('P102', N'102', 'STD', 'T1'),
       ('P103', N'103', 'DLX', 'T1'), ('P104', N'104', 'STE', 'T1'),
       ('P201', N'201', 'STD', 'T2'), ('P202', N'202', 'DLX', 'T2');

INSERT INTO KhachHang (MaKhachHang, HoTen, LoaiGiayTo, SoGiayTo, QuocGiaCap, SoDienThoai, Email, DiaChi)
VALUES ('KH00000001', N'Nguyễn Văn An', 'CCCD', '001234567890', 'VN', '0901234567', N'an@example.com', N'Hà Nội'),
       ('KH00000002', N'Trần Thị Bình', 'CCCD', '001234567891', 'VN', '0912345678', N'binh@example.com', N'Hà Nội'),
       ('KH00000003', N'Lê Văn Cường', NULL, NULL, NULL, '0923456789', N'cuong@example.com', N'Hải Phòng');
-- Tranh Java sinh trung ba ma khach mau.
ALTER SEQUENCE SeqKhachHang RESTART WITH 4;

INSERT INTO ChiTietPhong (MaPhong, MoTa)
VALUES ('P101', N'Phòng tiêu chuẩn, cửa sổ hướng sân.'), ('P103', N'Phòng Deluxe cho gia đình.');
INSERT INTO PhongTienNghi (MaPhong, MaTienNghi, TenTienNghi, SoLuong)
VALUES ('P101', 'TV', N'Tivi', 1), ('P101', 'DH', N'Điều hòa', 1);
INSERT INTO DichVu (MaDichVu, TenDichVu, DonGia, DonViTinh)
VALUES ('DV001', N'Nước uống', 20000, N'Chai');
GO
