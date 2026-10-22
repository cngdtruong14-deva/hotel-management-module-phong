USE HotelManagementDB_LocalSimple;
GO

-- TUY CHON: vi du SQL de hieu cac buoc Java Service can lam.
-- Chi dung voi du lieu mau moi; ROLLBACK cuoi file giu database nhu ban dau.
-- Day khong phai thu tuc hoan chinh: Java phai kiem tra phong trong, khach hoat dong,
-- trang thai hop le va dung ma truoc khi ghi, trong cung transaction.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. Tao mot luot va mot phieu giu P101.
INSERT INTO LuotLuuTru (MaLuotLuuTru, MaKhachHang) VALUES ('LT_DEMO', 'KH00000001');
INSERT INTO DatPhong (MaDatPhong, MaKhachHang, MaPhong, GiaPhongChot, MaLuotLuuTru)
VALUES ('DP_DEMO1', 'KH00000001', 'P101', 650000, 'LT_DEMO');
UPDATE Phong SET TrangThai = 'DA_DAT' WHERE MaPhong = 'P101';

-- 2. Nhan phong.
UPDATE DatPhong SET TrangThai = 'DANG_O', NgayNhan = SYSDATETIME() WHERE MaDatPhong = 'DP_DEMO1';
UPDATE Phong SET TrangThai = 'DANG_SU_DUNG' WHERE MaPhong = 'P101';
INSERT INTO PhongLuuTruNhan (MaDatPhong, MaPhong, MaKhachHang, TrangThai)
VALUES ('DP_DEMO1', 'P101', 'KH00000001', 'DANG_O');

-- 3. Doi sang P103: giu phieu P101 cu de khong lam sai lich su phan anh/dich vu.
DECLARE @ThoiGianChuyen DATETIME2(0) = SYSDATETIME();
UPDATE DatPhong SET TrangThai = 'DA_TRA', NgayTra = @ThoiGianChuyen WHERE MaDatPhong = 'DP_DEMO1';
UPDATE Phong SET TrangThai = 'DANG_DON' WHERE MaPhong = 'P101';
INSERT INTO DatPhong (MaDatPhong, MaKhachHang, MaPhong, GiaPhongChot, NgayNhan, TrangThai, MaLuotLuuTru)
VALUES ('DP_DEMO2', 'KH00000001', 'P103', 950000, @ThoiGianChuyen, 'DANG_O', 'LT_DEMO');
UPDATE Phong SET TrangThai = 'DANG_SU_DUNG' WHERE MaPhong = 'P103';
INSERT INTO ChuyenPhong (MaChuyenPhong, MaDatPhongCu, MaDatPhongMoi, LyDo, NguoiThucHien, ThoiGian)
VALUES ('CP_DEMO', 'DP_DEMO1', 'DP_DEMO2', N'Khách muốn phòng rộng hơn', N'Nhân viên mẫu', @ThoiGianChuyen);
UPDATE PhongLuuTruNhan SET TrangThai = 'DA_TRA', PhienBan = PhienBan + 1, CapNhatLuc = @ThoiGianChuyen WHERE MaDatPhong = 'DP_DEMO1';
INSERT INTO PhongLuuTruNhan (MaDatPhong, MaPhong, MaKhachHang, TrangThai)
VALUES ('DP_DEMO2', 'P103', 'KH00000001', 'DANG_O');

-- 4. Tra phong dang o.
UPDATE DatPhong SET TrangThai = 'DA_TRA', NgayTra = SYSDATETIME() WHERE MaDatPhong = 'DP_DEMO2';
UPDATE Phong SET TrangThai = 'DANG_DON' WHERE MaPhong = 'P103';
UPDATE PhongLuuTruNhan SET TrangThai = 'DA_TRA', PhienBan = PhienBan + 1, CapNhatLuc = SYSDATETIME() WHERE MaDatPhong = 'DP_DEMO2';
SELECT * FROM DatPhong WHERE MaLuotLuuTru = 'LT_DEMO';
SELECT * FROM ChuyenPhong WHERE MaChuyenPhong = 'CP_DEMO';
ROLLBACK;
GO
