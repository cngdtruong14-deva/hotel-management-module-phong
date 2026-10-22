USE HotelManagementDB_LocalSimple;
GO

-- Buoc 5: Xem du lieu vua tao.
SELECT * FROM LoaiPhong;
SELECT * FROM Phong;
SELECT * FROM KhachHang;
SELECT * FROM DatPhong; -- Chua co dong nao sau file 03.

-- Danh sach phong kem ten loai va gia hien tai.
SELECT p.MaPhong, p.SoPhong, l.TenLoai, t.TenTang,
       COALESCE(p.GiaPhong, l.GiaTheoNgay) AS GiaHienHanh, p.TrangThai
FROM Phong p
JOIN LoaiPhong l ON p.MaLoaiPhong = l.MaLoaiPhong
JOIN Tang t ON p.MaTang = t.MaTang;

-- Cac man hinh quan ly phong.
SELECT * FROM PhongTienNghi;
SELECT * FROM YeuCauDatPhong;
SELECT * FROM PhongDanhGiaNhan;
SELECT * FROM PhongPhanAnhNhan;
SELECT d.MaDatPhong, d.MaPhong, k.HoTen, d.NgayNhan
FROM DatPhong d JOIN KhachHang k ON d.MaKhachHang = k.MaKhachHang
WHERE d.TrangThai = 'DANG_O';

-- Lich su chuyen phong va dich vu theo luot.
SELECT * FROM ChuyenPhong;
SELECT d.MaLuotLuuTru, d.MaDatPhong, d.MaPhong, d.NgayNhan, d.NgayTra, d.TrangThai
FROM DatPhong d ORDER BY d.NgayDat;
SELECT d.MaLuotLuuTru, s.* FROM SuDungDichVu s
JOIN DatPhong d ON s.MaDatPhong = d.MaDatPhong;
GO
