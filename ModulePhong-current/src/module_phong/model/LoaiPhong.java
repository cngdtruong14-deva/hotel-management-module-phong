package module_phong.model;
import java.math.BigDecimal;
public record LoaiPhong(String maLoaiPhong, String tenLoai, int soNguoiToiDa, BigDecimal giaTheoNgay) {
    public String toString() { return maLoaiPhong + " · " + tenLoai; }
}
