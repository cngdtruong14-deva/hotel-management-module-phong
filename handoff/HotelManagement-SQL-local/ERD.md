# Quan hệ dữ liệu đơn giản

```mermaid
erDiagram
    Tang ||--o{ Phong : MaTang
    LoaiPhong ||--o{ Phong : MaLoaiPhong
    KhachHang ||--o{ DatPhong : MaKhachHang
    Phong ||--o{ DatPhong : MaPhong
    LuotLuuTru o|--o{ DatPhong : MaLuotLuuTru
    DatPhong ||--o{ ChuyenPhong : phieu_cu_va_moi
    Phong ||--o{ PhongTienNghi : MaPhong
    Phong ||--o| ChiTietPhong : MaPhong
    KhachHang ||--o{ YeuCauDatPhong : MaKhachHang
    Phong ||--o{ YeuCauDatPhong : MaPhong
    DatPhong o|--o{ YeuCauDatPhong : ket_qua
    DatPhong ||--o| PhongLuuTruNhan : MaDatPhong
    DatPhong ||--o{ PhongDanhGiaNhan : MaDatPhong
    DatPhong ||--o{ PhongPhanAnhNhan : MaDatPhong
    Phong ||--o{ VeSinh : MaPhong
    Phong ||--o{ BaoTri : MaPhong
    DatPhong ||--o{ SuDungDichVu : MaDatPhong
    DichVu ||--o{ SuDungDichVu : MaDichVu
```

Các FK đơn được khai báo trực tiếp cạnh cột trong file01/02. Java kiểm tra thêm các mã khách/phòng/phiếu thuộc cùng nguồn. Danh sách review một/phiếu được kiểm tra bởi service, không phải UNIQUE SQL trong bản đơn giản.
