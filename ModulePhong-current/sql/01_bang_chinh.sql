USE HotelManagementDB_LocalSimple;
GO

-- Buoc 2: Cac bang chinh cua Phong, Khach hang va Dat phong.
-- Chi chay mot lan tren database moi.

-- 1. Tang: giu rieng vi giao dien hien co chuc nang quan ly tang.
CREATE TABLE Tang (
    MaTang VARCHAR(20) PRIMARY KEY,
    TenTang NVARCHAR(100) NOT NULL
);

-- 2. Loai phong
CREATE TABLE LoaiPhong (
    MaLoaiPhong VARCHAR(20) PRIMARY KEY,
    TenLoai NVARCHAR(100) NOT NULL,
    GiaTheoNgay DECIMAL(18,2) NOT NULL,
    SoNguoiToiDa INT NOT NULL
);

-- 3. Phong
-- Trang thai: TRONG, DA_DAT, DANG_SU_DUNG, DANG_DON, BAO_TRI.
CREATE TABLE Phong (
    MaPhong VARCHAR(20) PRIMARY KEY,
    SoPhong NVARCHAR(20) NOT NULL,
    MaLoaiPhong VARCHAR(20) NOT NULL REFERENCES LoaiPhong(MaLoaiPhong),
    MaTang VARCHAR(20) NOT NULL REFERENCES Tang(MaTang),
    GiaPhong DECIMAL(18,2) NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'TRONG',
    NgungSuDung BIT NOT NULL DEFAULT 0
);
-- GiaPhong de NULL thi lay gia trong LoaiPhong.

-- 4. Khach hang: giu du 13 thuoc tinh cua module khach hang da lam.
CREATE TABLE KhachHang (
    MaKhachHang VARCHAR(20) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    LoaiGiayTo VARCHAR(10) NULL,
    SoGiayTo VARCHAR(30) NULL,
    QuocGiaCap CHAR(2) NULL,
    SoDienThoai VARCHAR(20) NULL,
    Email NVARCHAR(254) NULL,
    NgaySinh DATE NULL,
    GioiTinh VARCHAR(15) NOT NULL DEFAULT 'KHONG_CUNG_CAP',
    QuocTich CHAR(2) NULL,
    DiaChi NVARCHAR(255) NULL,
    NgayTao DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG'
);
-- Giu quy tac chong trung giay to cua module khach hang goc.
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
CREATE UNIQUE INDEX UX_KhachHang_GiayTo
ON dbo.KhachHang(LoaiGiayTo, QuocGiaCap, SoGiayTo)
WHERE SoGiayTo IS NOT NULL;

-- Java hien co dung sequence nay de sinh KH00000001, KH00000002,...
CREATE SEQUENCE SeqKhachHang AS BIGINT START WITH 1 INCREMENT BY 1;

-- 5. Mot luot luu tru chung; dung de noi cac phieu khi khach doi phong.
CREATE TABLE LuotLuuTru (
    MaLuotLuuTru VARCHAR(20) PRIMARY KEY,
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    TaoLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

-- 6. Phieu dat phong (moi phieu gan mot phong).
-- Trang thai: GIU_PHONG, DANG_O, DA_TRA, DA_HUY.
CREATE TABLE DatPhong (
    MaDatPhong VARCHAR(20) PRIMARY KEY,
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    GiaPhongChot DECIMAL(18,2) NOT NULL,
    NgayDat DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    NgayNhan DATETIME2(0) NULL,
    NgayTra DATETIME2(0) NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'GIU_PHONG',
    MaLuotLuuTru VARCHAR(20) NULL REFERENCES LuotLuuTru(MaLuotLuuTru)
);
-- NgayNhan/NgayTra la thoi diem thuc te; chua nhan/tra thi NULL.
-- GiaPhongChot giu gia tai luc dat, khong thay khi danh muc tang gia.
-- MaLuotLuuTru de NULL van tuong thich Java cu; khi doi phong Java tao nhom.

-- 7. Lich su doi phong: giu phieu cu, tao phieu phong moi cung luot.
CREATE TABLE ChuyenPhong (
    MaChuyenPhong VARCHAR(20) PRIMARY KEY,
    MaDatPhongCu VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    MaDatPhongMoi VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    LyDo NVARCHAR(1000) NOT NULL,
    NguoiThucHien NVARCHAR(100) NOT NULL,
    ThoiGian DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);
GO
