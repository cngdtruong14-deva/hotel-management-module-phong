package common;
import java.sql.*;
/** Fail before opening the UI when an older schema is selected. */
public final class SchemaContract {
 private SchemaContract() {}
 public static void verify(ConnectionFactory db) throws SQLException {
  try(var c=db.open(); var s=c.createStatement()) {
   s.executeQuery("SELECT TOP 0 MaKhachHang,HoTen,LoaiGiayTo,SoGiayTo,QuocGiaCap,SoDienThoai,Email,NgaySinh,GioiTinh,QuocTich,DiaChi,NgayTao,TrangThai FROM dbo.KhachHang").close();
   s.executeQuery("SELECT TOP 0 MaDatPhong,MaKhachHang,MaPhong,MaLuotLuuTru FROM dbo.DatPhong").close();
   s.executeQuery("SELECT TOP 0 MaSuDung,MaDatPhong,MaDichVu FROM dbo.SuDungDichVu").close();
   s.executeQuery("SELECT TOP 0 MaPhong,MoTa FROM dbo.ChiTietPhong").close();
   s.executeQuery("SELECT TOP 0 MaPhong,MaTienNghi,TenTienNghi,SoLuong,TinhTrang FROM dbo.PhongTienNghi").close();
   s.executeQuery("SELECT TOP 0 MaYeuCau,MaKhachHang,MaPhong,GhiChu,TrangThai,MaDatPhong,LyDo,PhienBan,TaoLuc,CapNhatLuc FROM dbo.YeuCauDatPhong").close();
   s.executeQuery("SELECT TOP 0 MaDatPhong,MaPhong,MaKhachHang,TrangThai,PhienBan,CapNhatLuc FROM dbo.PhongLuuTruNhan").close();
   s.executeQuery("SELECT TOP 0 MaDanhGia,MaDatPhong,MaPhong,MaKhachHang,Diem,NoiDung,TaoLuc FROM dbo.PhongDanhGiaNhan").close();
   s.executeQuery("SELECT TOP 0 MaPhanAnh,MaDatPhong,MaPhong,MaKhachHang,NoiDung,TrangThai,PhienBan,CapNhatLuc FROM dbo.PhongPhanAnhNhan").close();
   s.executeQuery("SELECT TOP 0 MaLuotLuuTru,MaKhachHang,TaoLuc FROM dbo.LuotLuuTru").close();
   s.executeQuery("SELECT TOP 0 MaChuyenPhong,MaDatPhongCu,MaDatPhongMoi FROM dbo.ChuyenPhong").close();
   try(var r=s.executeQuery("SELECT COUNT(*) FROM sys.sequences WHERE object_id=OBJECT_ID('dbo.SeqKhachHang')")) {
    r.next(); if(r.getInt(1)!=1) throw new SQLException("Missing SeqKhachHang");
   }
  } catch(SQLException e) { throw new SQLException("Database chưa đúng hợp đồng room-simple-1. Đọc README: chạy bộ SQL đơn giản 00, 01, 02 trên database mới; giữ khách13.",e); }
 }
}
