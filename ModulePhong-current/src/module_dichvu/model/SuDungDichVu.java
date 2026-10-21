package module_dichvu.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record SuDungDichVu(String maSuDung, String maDatPhong, String maDichVu,
        int soLuong, BigDecimal donGia, LocalDateTime thoiGianSuDung) {
    public BigDecimal thanhTien() { return donGia.multiply(BigDecimal.valueOf(soLuong)); }
}
