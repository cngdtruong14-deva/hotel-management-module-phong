package module_datphong.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Minimal immediate reservation/stay, not future date scheduling. */
public record DatPhong(String maDatPhong, String maKhachHang, String maPhong,
        BigDecimal giaPhongChot, LocalDateTime ngayDat, LocalDateTime ngayNhan,
        LocalDateTime ngayTra, TrangThaiDatPhong trangThai) {
    public String toString() { return maDatPhong + " · " + maKhachHang + " · " + maPhong; }
}
