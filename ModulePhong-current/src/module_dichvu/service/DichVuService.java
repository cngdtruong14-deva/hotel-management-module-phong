package module_dichvu.service;

import module_dichvu.model.*;
import module_lienket.service.LienKetService;
import java.math.BigDecimal;
import java.util.List;

public class DichVuService {
    private final LienKetService lienKet;

    public DichVuService(LienKetService lienKet) {
        this.lienKet = lienKet;
    }

    public List<DichVu> getDichVus() {
        return lienKet.taiDuLieu().dichVus();
    }

    public DichVu getDichVu(String maDichVu) {
        return lienKet.getDichVu(maDichVu);
    }

    public void luuDichVu(DichVu dichVu, boolean sua) {
        lienKet.luuDichVu(dichVu, sua);
    }

    public void ghiNhanDichVu(String maDatPhong, String maDichVu, int soLuong) {
        lienKet.ghiNhanDichVu(maDatPhong, maDichVu, soLuong);
    }

    public List<SuDungDichVu> getDichVuTheoDatPhong(String maDatPhong) {
        return lienKet.getDichVuTheoDatPhong(maDatPhong);
    }

    public BigDecimal getTienDichVu(String maDatPhong) {
        return lienKet.getTienDichVu(maDatPhong);
    }
}
