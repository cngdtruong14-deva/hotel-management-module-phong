package module_khachhang.dao;

import java.time.LocalDateTime;
import java.util.List;
import module_khachhang.model.*;

public interface KhachHangDAO {
    List<KhachHang> danhSach();
    KhachHang get(String ma);
    KhachHang them(DuLieuKhachHang data, LocalDateTime ngayTao);
    void sua(String ma, DuLieuKhachHang data);
    void doiTrangThai(String ma, TrangThaiKhachHang status);
}
