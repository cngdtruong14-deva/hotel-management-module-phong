package module_phong.service;

import module_phong.model.HoSoPhong.*;
import java.util.List;

/** Cong de module dat phong gui ket qua; ben goi phai kiem tra quyen thao tac. */
public interface CongLuuTru {
    List<YeuCau> yeuCauChoXuLy();

    void traKetQua(
            String maYeuCau, String trangThai, String maDatPhong, String lyDo, long phienBan);

    void nhanTrangThai(LuuTru message);

    void nhanDanhGia(DanhGia message);

    void nhanPhanAnh(PhanAnh message);
}
