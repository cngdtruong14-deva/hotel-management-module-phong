USE HotelManagementDB_LocalSimple;
GO

-- Buoc 3: Cac bang phuc vu chuc nang giao dien con lai.

-- 1. Mo ta va tien nghi cua phong
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

-- 2. Yeu cau gui tu quan ly phong sang dat phong/luu tru
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

-- 3. Trang thai luu tru nhan ve de giao dien phong theo doi
CREATE TABLE PhongLuuTruNhan (
    MaDatPhong VARCHAR(20) PRIMARY KEY REFERENCES DatPhong(MaDatPhong),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    TrangThai VARCHAR(20) NOT NULL,
    PhienBan BIGINT NOT NULL DEFAULT 1,
    CapNhatLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

-- 4. Review va diem danh gia cua khach
CREATE TABLE PhongDanhGiaNhan (
    MaDanhGia VARCHAR(20) PRIMARY KEY,
    MaDatPhong VARCHAR(20) NOT NULL REFERENCES DatPhong(MaDatPhong),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    Diem INT NOT NULL,
    NoiDung NVARCHAR(2000) NOT NULL,
    TaoLuc DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);
-- Java kiem tra diem 1..5, dung khach/phong, phieu da tra va mot review/phieu.

-- 5. Phan anh trong tung phong
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
-- PhienBan o cac bang nhan giu de tuong thich service Java hien co.

-- 6. Ve sinh va bao tri
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

-- 7. Danh muc va dich vu da su dung (giu lien ket cac module da co)
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
-- ThanhTien tu tinh de Java hien tai van doc duoc.
GO
