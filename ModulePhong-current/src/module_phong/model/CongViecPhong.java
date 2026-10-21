package module_phong.model;
import java.time.LocalDateTime;
/** Maps to VeSinh or BaoTri. loai is VESINH or BAOTRI. */
public record CongViecPhong(String maCongViec, String loai, String maPhong, String noiDung,
        String nguoiPhuTrach, LocalDateTime batDau, LocalDateTime ketThuc) {
    public boolean dangThucHien() { return ketThuc == null; }
}
