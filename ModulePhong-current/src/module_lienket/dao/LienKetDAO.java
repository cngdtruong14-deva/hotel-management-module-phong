package module_lienket.dao;
import module_phong.dao.PhongDAO;
import module_lienket.model.DuLieuLienKet;
import module_khachhang.model.KhachHang;
import module_dichvu.model.*;
import module_datphong.model.DatPhong;
import java.util.function.Function;

/** Shared integration contract; implementation owns one transaction across all tables. */
public interface LienKetDAO {
    <T> T giaoDich(Function<Session,T> work);
    interface Session {
        PhongDAO.Session phongSession();
        DuLieuLienKet doc();
        void luuDichVu(DichVu dv, boolean sua);
        void luuDatPhong(DatPhong dp, boolean sua);
        void themSuDung(SuDungDichVu sd);
    }
}
