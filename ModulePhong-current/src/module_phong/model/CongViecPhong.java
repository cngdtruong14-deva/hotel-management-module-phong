package module_phong.model;

import java.time.LocalDateTime;

/** Cong viec ve sinh hoac bao tri; loai nhan gia tri VESINH hoac BAOTRI. */
public record CongViecPhong(
        String maCongViec,
        String loai,
        String maPhong,
        String noiDung,
        String nguoiPhuTrach,
        LocalDateTime batDau,
        LocalDateTime ketThuc) {
    public boolean dangThucHien() {
        return ketThuc == null;
    }
}
