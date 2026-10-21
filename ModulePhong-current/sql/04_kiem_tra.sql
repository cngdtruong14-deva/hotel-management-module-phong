USE HotelManagementDB_LocalSimple;
GO

-- Bước 5: Xem dữ liệu vừa tạo.
SELECT * FROM LoaiPhong;
SELECT * FROM Phong;
SELECT * FROM KhachHang;
SELECT * FROM DatPhong; -- Chưa có dòng nào sau file 03.

-- Danh sách phòng kèm tên loại và giá hiện tại.
SELECT p.MaPhong, p.SoPhong, l.TenLoai, t.TenTang,
       COALESCE(p.GiaPhong, l.GiaTheoNgay) AS GiaHienHanh, p.TrangThai
FROM Phong p
JOIN LoaiPhong l ON p.MaLoaiPhong = l.MaLoaiPhong
JOIN Tang t ON p.MaTang = t.MaTang;

-- Các màn hình quản lý phòng.
SELECT * FROM PhongTienNghi;
SELECT * FROM YeuCauDatPhong;
SELECT * FROM PhongDanhGiaNhan;
SELECT * FROM PhongPhanAnhNhan;
SELECT d.MaDatPhong, d.MaPhong, k.HoTen, d.NgayNhan
FROM DatPhong d JOIN KhachHang k ON d.MaKhachHang = k.MaKhachHang
WHERE d.TrangThai = 'DANG_O';

-- Lịch sử chuyển phòng và dịch vụ theo lượt.
SELECT * FROM ChuyenPhong;
SELECT d.MaLuotLuuTru, d.MaDatPhong, d.MaPhong, d.NgayNhan, d.NgayTra, d.TrangThai
FROM DatPhong d ORDER BY d.NgayDat;
SELECT d.MaLuotLuuTru, s.* FROM SuDungDichVu s
JOIN DatPhong d ON s.MaDatPhong = d.MaDatPhong;
GO
