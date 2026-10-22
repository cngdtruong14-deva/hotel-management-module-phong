package module_lienket.model;

import module_khachhang.model.KhachHang;
import module_dichvu.model.*;
import module_datphong.model.DatPhong;
import java.util.List;

public record DuLieuLienKet(
        List<KhachHang> khachHangs,
        List<DichVu> dichVus,
        List<DatPhong> datPhongs,
        List<SuDungDichVu> suDungs) {
    public DuLieuLienKet {
        khachHangs = List.copyOf(khachHangs);
        dichVus = List.copyOf(dichVus);
        datPhongs = List.copyOf(datPhongs);
        suDungs = List.copyOf(suDungs);
    }

    public static DuLieuLienKet rong() {
        return new DuLieuLienKet(List.of(), List.of(), List.of(), List.of());
    }
}
