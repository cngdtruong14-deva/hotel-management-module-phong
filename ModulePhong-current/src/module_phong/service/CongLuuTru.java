package module_phong.service;
import module_phong.model.HoSoPhong.*;
import java.util.List;

/** Called by the authenticated/in-process booking adapter, never by room UI. */
public interface CongLuuTru {
 List<YeuCau> yeuCauChoXuLy();
 void traKetQua(String maYeuCau,String trangThai,String maDatPhong,String lyDo,long phienBan);
 void nhanTrangThai(LuuTru message);
 void nhanDanhGia(DanhGia message);
 void nhanPhanAnh(PhanAnh message);
}
