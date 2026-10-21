USE HotelManagementDB_LocalSimple;
GO

-- Bước 3: Các bảng phục vụ chức năng giao diện còn lại.

-- 1. Mô tả và tiện nghi của phòng
CREATE TABLE ChiTietPhong (
    MaPhong VARCHAR(20) PRIMARY KEY REFERENCES Phong(MaPhong),
    MoTa NVARCHAR(2000) NOT NULL DEFAULT N''
);
CREATE TABLE PhongTienNghi (
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    MaTienNghi VARCHAR(20) NOT NULL,
    TenTienNghi NVARCHAR(100) NOT NULL,
    SoLuong INT NOT NULL DEFAULT 1,
    TinhTrang VARCHAR(20) NOT NULL DEFAULT 'TOT',
    PRIMARY KEY (MaPhong, MaTienNghi)
);
-- TinhTrang: TOT, CAN_SUA, NGUNG_DUNG.

-- 2. Yêu cầu gửi từ quản lý phòng sang đặt phòng/lưu trú
CREATE TABLE YeuCauDatPhong (
    MaYeuCau VARCHAR(20) PRIMARY KEY,
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    GhiChu NVARCHAR(1000) NOT NULL DEFAULT N'',
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'CHO_XU_LY',
    MaDatPhong VARCHAR(20) NULL REFERENCES DatPhong(MaDatPhong),
    LyDo NVARCHAR(1000) NOT NULL DEFAULT N'',
    PhienBan BIGINT NOT NULL DEFAULT 0,
    TaoLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    CapNhatLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);
-- TrangThai: CHO_XU_LY, TIEP_NHAN, TU_CHOI, HOAN_TAT.

-- 3. Trạng thái lưu trú nhận về để giao diện phòng theo dõi
CREATE TABLE PhongLuuTruNhan (
    MaDatPhong VARCHAR(20) PRIMARY KEY REFERENCES DatPhong(MaDatPhong),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    TrangThai VARCHAR(20) NOT NULL,
    PhienBan BIGINT NOT NULL DEFAULT 1,
    CapNhatLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

-- 4. Review và điểm đánh giá của khách
CREATE TABLE PhongDanhGiaNhan (
    MaDanhGia VARCHAR(20) PRIMARY KEY,
    MaDatPhong VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    Diem INT NOT NULL,
    NoiDung NVARCHAR(2000) NOT NULL,
    TaoLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);
-- Java kiểm tra điểm 1..5, đúng khách/phòng, phiếu đã trả và một review/phiếu.

-- 5. Phản ánh trong từng phòng
CREATE TABLE PhongPhanAnhNhan (
    MaPhanAnh VARCHAR(20) PRIMARY KEY,
    MaDatPhong VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    NoiDung NVARCHAR(2000) NOT NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'MOI',
    PhienBan BIGINT NOT NULL DEFAULT 1,
    CapNhatLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);
-- TrangThai: MOI, DANG_XU_LY, DA_XU_LY.
-- PhienBan ở các bảng nhận giữ để tương thích service Java hiện có.

-- 6. Vệ sinh và bảo trì
CREATE TABLE VeSinh (
    MaVeSinh VARCHAR(36) PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    NoiDung NVARCHAR(500) NOT NULL,
    NguoiPhuTrach NVARCHAR(100) NOT NULL,
    BatDau DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    KetThuc DATETIME2(0) NULL
);
CREATE TABLE BaoTri (
    MaBaoTri VARCHAR(36) PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    NoiDung NVARCHAR(500) NOT NULL,
    NguoiPhuTrach NVARCHAR(100) NOT NULL,
    BatDau DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    KetThuc DATETIME2(0) NULL
);

-- 7. Danh mục và dịch vụ đã sử dụng (giữ liên kết các module đã có)
CREATE TABLE DichVu (
    MaDichVu VARCHAR(20) PRIMARY KEY,
    TenDichVu NVARCHAR(100) NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    DonViTinh NVARCHAR(30) NOT NULL,
    NgungSuDung BIT NOT NULL DEFAULT 0
);
CREATE TABLE SuDungDichVu (
    MaSuDung VARCHAR(36) PRIMARY KEY,
    MaDatPhong VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    MaDichVu VARCHAR(20) NOT NULL REFERENCES DichVu(MaDichVu),
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThoiGianSuDung DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    ThanhTien AS CAST(SoLuong * DonGia AS DECIMAL(18,2))
);
-- ThanhTien tự tính để Java hiện tại vẫn đọc được.
GO
