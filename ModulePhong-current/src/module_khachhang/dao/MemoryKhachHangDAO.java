package module_khachhang.dao;

import java.time.LocalDateTime;
import java.util.*;
import module_khachhang.model.*;

/** Chế độ demo: dữ liệu chỉ tồn tại trong lần chạy hiện tại. */
public final class MemoryKhachHangDAO implements KhachHangDAO {
    private final Map<String, KhachHang> rows = new LinkedHashMap<>();
    private long sequence;

    @Override public synchronized List<KhachHang> danhSach() { return List.copyOf(rows.values()); }
    @Override public synchronized KhachHang get(String ma) {
        var p = rows.get(ma);
        if (p == null) throw new IllegalArgumentException("Không tìm thấy khách hàng: " + ma);
        return p;
    }
    private void checkDocument(String except, DuLieuKhachHang d) {
        if (d.soGiayTo() == null) return;
        for (var p : rows.values()) {
            if (!p.maKhachHang().equals(except) && p.loaiGiayTo() == d.loaiGiayTo()
                    && Objects.equals(p.quocGiaCap(), d.quocGiaCap())
                    && Objects.equals(p.soGiayTo(), d.soGiayTo()))
                throw new IllegalArgumentException("Giấy tờ đã có trong hồ sơ " + p.maKhachHang());
        }
    }
    @Override public synchronized KhachHang them(DuLieuKhachHang d, LocalDateTime tao) {
        checkDocument(null, d);
        String ma = String.format(Locale.ROOT, "KH%08d", ++sequence);
        var p = d.thanhHoSo(ma, tao, TrangThaiKhachHang.HOAT_DONG);
        rows.put(ma, p);
        return p;
    }
    @Override public synchronized void sua(String ma, DuLieuKhachHang d) {
        var old = get(ma);
        checkDocument(ma, d);
        rows.put(ma, d.thanhHoSo(ma, old.ngayTao(), old.trangThai()));
    }
    @Override public synchronized void doiTrangThai(String ma, TrangThaiKhachHang status) {
        var old = get(ma);
        rows.put(ma, DuLieuKhachHang.tu(old).thanhHoSo(ma, old.ngayTao(), status));
    }
}
