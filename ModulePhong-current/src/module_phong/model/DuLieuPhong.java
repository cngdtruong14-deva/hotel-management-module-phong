package module_phong.model;

import java.util.List;

public record DuLieuPhong(
        List<Phong> phongs,
        List<LoaiPhong> loais,
        List<Tang> tangs,
        List<CongViecPhong> congViecs) {
    public DuLieuPhong {
        phongs = List.copyOf(phongs);
        loais = List.copyOf(loais);
        tangs = List.copyOf(tangs);
        congViecs = List.copyOf(congViecs);
    }

    public static DuLieuPhong rong() {
        return new DuLieuPhong(List.of(), List.of(), List.of(), List.of());
    }
}
