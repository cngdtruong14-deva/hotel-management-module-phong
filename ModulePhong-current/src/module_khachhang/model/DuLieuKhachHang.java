package module_khachhang.model;

import java.time.LocalDate;

/** Dữ liệu thêm/sửa; mã, ngày tạo và trạng thái được quản lý riêng. */
public record DuLieuKhachHang(String hoTen, LoaiGiayTo loaiGiayTo, String soGiayTo,
        String quocGiaCap, String soDienThoai, String email, LocalDate ngaySinh,
        GioiTinh gioiTinh, String quocTich, String diaChi) {
    public static DuLieuKhachHang tu(KhachHang p) {
        return new DuLieuKhachHang(p.hoTen(), p.loaiGiayTo(), p.soGiayTo(), p.quocGiaCap(),
                p.soDienThoai(), p.email(), p.ngaySinh(), p.gioiTinh(), p.quocTich(), p.diaChi());
    }
    public KhachHang thanhHoSo(String ma, java.time.LocalDateTime tao, TrangThaiKhachHang status) {
        return new KhachHang(ma, hoTen, loaiGiayTo, soGiayTo, quocGiaCap, soDienThoai,
                email, ngaySinh, gioiTinh, quocTich, diaChi, tao, status);
    }
}
