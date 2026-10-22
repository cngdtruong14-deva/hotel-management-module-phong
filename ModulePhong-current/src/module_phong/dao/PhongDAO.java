package module_phong.dao;

import module_phong.model.*;
import java.util.*;
import java.util.function.Function;

/** Cac thao tac trong session dung chung mot giao dich cua module phong. */
public interface PhongDAO {
    <T> T giaoDich(Function<Session, T> work);

    interface Session {
        List<Phong> phongs();

        List<LoaiPhong> loais();

        List<Tang> tangs();

        List<CongViecPhong> congViecs();

        void luuPhong(Phong p, boolean sua);

        void luuLoai(LoaiPhong p, boolean sua);

        void luuTang(Tang p, boolean sua);

        void xoaLoai(String ma);

        void xoaTang(String ma);

        void themCongViec(CongViecPhong c);

        void ketThucCongViec(CongViecPhong c);
    }
}
