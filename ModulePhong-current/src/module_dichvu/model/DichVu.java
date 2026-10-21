package module_dichvu.model;
import java.math.BigDecimal;
public record DichVu(String maDichVu, String tenDichVu, BigDecimal donGia,
        String donViTinh, boolean ngungSuDung) {
    public String toString() { return maDichVu + " · " + tenDichVu; }
}
