package module_datphong.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Phieu dat va luu tru hien tai; chua quan ly lich dat phong trong tuong lai. */
public record DatPhong(
        String maDatPhong,
        String maKhachHang,
        String maPhong,
        BigDecimal giaPhongChot,
        LocalDateTime ngayDat,
        LocalDateTime ngayNhan,
        LocalDateTime ngayTra,
        TrangThaiDatPhong trangThai) {
    public String toString() {
        return maDatPhong + " · " + maKhachHang + " · " + maPhong;
    }
}
