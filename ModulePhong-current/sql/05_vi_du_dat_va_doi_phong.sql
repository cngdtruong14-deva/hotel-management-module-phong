USE HotelManagementDB_LocalSimple;
GO

-- TÙY CHỌN: ví dụ SQL để hiểu các bước Java Service cần làm.
-- Chỉ dùng với dữ liệu mẫu mới; ROLLBACK cuối file giữ database như ban đầu.
-- Đây không phải thủ tục hoàn chỉnh: Java phải kiểm tra phòng trống, khách hoạt động,
-- trạng thái hợp lệ và đúng mã trước khi ghi, trong cùng transaction.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. Tạo một lượt và một phiếu giữ P101.
INSERT INTO LuotLuuTru (MaLuotLuuTru, MaKhachHang) VALUES ('LT_DEMO', 'KH00000001');
INSERT INTO DatPhong (MaDatPhong, MaKhachHang, MaPhong, GiaPhongChot, MaLuotLuuTru)
VALUES ('DP_DEMO1', 'KH00000001', 'P101', 650000, 'LT_DEMO');
UPDATE Phong SET TrangThai = 'DA_DAT' WHERE MaPhong = 'P101';

-- 2. Nhận phòng.
UPDATE DatPhong SET TrangThai = 'DANG_O', NgayNhan = SYSDATETIME() WHERE MaDatPhong = 'DP_DEMO1';
UPDATE Phong SET TrangThai = 'DANG_SU_DUNG' WHERE MaPhong = 'P101';
INSERT INTO PhongLuuTruNhan (MaDatPhong, MaPhong, MaKhachHang, TrangThai)
VALUES ('DP_DEMO1', 'P101', 'KH00000001', 'DANG_O');

-- 3. Đổi sang P103: giữ phiếu P101 cũ để không làm sai lịch sử phản ánh/dịch vụ.
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

-- 4. Trả phòng đang ở.
UPDATE DatPhong SET TrangThai = 'DA_TRA', NgayTra = SYSDATETIME() WHERE MaDatPhong = 'DP_DEMO2';
UPDATE Phong SET TrangThai = 'DANG_DON' WHERE MaPhong = 'P103';
UPDATE PhongLuuTruNhan SET TrangThai = 'DA_TRA', PhienBan = PhienBan + 1, CapNhatLuc = SYSDATETIME() WHERE MaDatPhong = 'DP_DEMO2';
SELECT * FROM DatPhong WHERE MaLuotLuuTru = 'LT_DEMO';
SELECT * FROM ChuyenPhong WHERE MaChuyenPhong = 'CP_DEMO';
ROLLBACK;
GO
