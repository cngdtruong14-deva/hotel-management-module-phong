package module_phong.dao;

import module_phong.model.HoSoPhong.*;
import module_phong.model.Phong;
import module_khachhang.model.KhachHang;
import module_datphong.model.DatPhong;
import java.util.function.Function;

public interface HoSoPhongDAO {
    <T> T giaoDich(Function<Session, T> work);

    interface Session {
        KhachHang khach(String ma);

        Phong phong(String ma);

        DatPhong datPhong(String ma);

        DuLieu doc();

        void luu(ChiTiet p);

        void luu(TienNghi p);

        void luu(YeuCau p);

        void luu(LuuTru p);

        void luu(DanhGia p);

        void luu(PhanAnh p);
    }
}
