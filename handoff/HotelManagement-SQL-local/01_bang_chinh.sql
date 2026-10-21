USE HotelManagementDB_LocalSimple;
GO

-- Bước 2: Các bảng chính của Phòng, Khách hàng và Đặt phòng.
-- Chỉ chạy một lần trên database mới.

-- 1. Tầng: giữ riêng vì giao diện hiện có chức năng quản lý tầng.
CREATE TABLE Tang (
    MaTang VARCHAR(20) PRIMARY KEY,
    TenTang NVARCHAR(100) NOT NULL
);

-- 2. Loại phòng
CREATE TABLE LoaiPhong (
    MaLoaiPhong VARCHAR(20) PRIMARY KEY,
    TenLoai NVARCHAR(100) NOT NULL,
    GiaTheoNgay DECIMAL(18,2) NOT NULL,
    SoNguoiToiDa INT NOT NULL
);

-- 3. Phòng
-- Trạng thái: TRONG, DA_DAT, DANG_SU_DUNG, DANG_DON, BAO_TRI.
CREATE TABLE Phong (
    MaPhong VARCHAR(20) PRIMARY KEY,
    SoPhong NVARCHAR(20) NOT NULL,
    MaLoaiPhong VARCHAR(20) NOT NULL REFERENCES LoaiPhong(MaLoaiPhong),
    MaTang VARCHAR(20) NOT NULL REFERENCES Tang(MaTang),
    GiaPhong DECIMAL(18,2) NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'TRONG',
    NgungSuDung BIT NOT NULL DEFAULT 0
);
-- GiaPhong để NULL thì lấy giá trong LoaiPhong.

-- 4. Khách hàng: giữ đủ 13 thuộc tính của module khách hàng đã làm.
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
-- Giữ quy tắc chống trùng giấy tờ của module khách hàng gốc.
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

-- Java hiện có dùng sequence này để sinh KH00000001, KH00000002,...
CREATE SEQUENCE SeqKhachHang AS BIGINT START WITH 1 INCREMENT BY 1;

-- 5. Một lượt lưu trú chung; dùng để nối các phiếu khi khách đổi phòng.
CREATE TABLE LuotLuuTru (
    MaLuotLuuTru VARCHAR(20) PRIMARY KEY,
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    TaoLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

-- 6. Phiếu đặt phòng (mỗi phiếu gắn một phòng).
-- Trạng thái: GIU_PHONG, DANG_O, DA_TRA, DA_HUY.
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
-- NgayNhan/NgayTra là thời điểm thực tế; chưa nhận/trả thì NULL.
-- GiaPhongChot giữ giá tại lúc đặt, không thay khi danh mục tăng giá.
-- MaLuotLuuTru để NULL vẫn tương thích Java cũ; khi đổi phòng Java tạo nhóm.

-- 7. Lịch sử đổi phòng: giữ phiếu cũ, tạo phiếu phòng mới cùng lượt.
CREATE TABLE ChuyenPhong (
    MaChuyenPhong VARCHAR(20) PRIMARY KEY,
    MaDatPhongCu VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    MaDatPhongMoi VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    LyDo NVARCHAR(1000) NOT NULL,
    NguoiThucHien NVARCHAR(100) NOT NULL,
    ThoiGian DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);
GO
