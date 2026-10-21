package module_khachhang.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Hồ sơ bất biến; validation ở Service, ngày tạo theo giờ khách sạn. */
public record KhachHang(
        String maKhachHang,
        String hoTen,
        LoaiGiayTo loaiGiayTo,
        String soGiayTo,
        String quocGiaCap,
        String soDienThoai,
        String email,
        LocalDate ngaySinh,
        GioiTinh gioiTinh,
        String quocTich,
        String diaChi,
        LocalDateTime ngayTao,
        TrangThaiKhachHang trangThai) {
    @Override public String toString() { return maKhachHang + " · " + hoTen; }
    public String sdt() {
        return soDienThoai;
    }

    /** CCCD không chứa số Passport của khách nước ngoài. */
    public String cccd() {
        return loaiGiayTo == LoaiGiayTo.CCCD ? soGiayTo : null;
    }
}
