package module_khachhang.model;

import java.util.Locale;

/** Giá trị null của bộ lọc nghĩa là tất cả. */
public record BoLocKhachHang(String tuKhoa, GioiTinh gioiTinh, String quocTich,
        TrangThaiKhachHang trangThai) {
    public boolean khop(KhachHang p) {
        String key = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(Locale.ROOT);
        if (gioiTinh != null && gioiTinh != p.gioiTinh()) return false;
        if (trangThai != null && trangThai != p.trangThai()) return false;
        if (quocTich != null && !quocTich.isBlank() && !quocTich.equalsIgnoreCase(p.quocTich())) return false;

        return chuaTuKhoa(p.maKhachHang(), key) || chuaTuKhoa(p.hoTen(), key)
                || chuaTuKhoa(p.soGiayTo(), key) || chuaTuKhoa(p.soDienThoai(), key);
    }

    private static boolean chuaTuKhoa(String noiDung, String tuKhoa) {
        return noiDung != null && noiDung.toLowerCase(Locale.ROOT).contains(tuKhoa);
    }
}
