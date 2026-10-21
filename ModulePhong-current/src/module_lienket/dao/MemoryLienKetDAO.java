package module_lienket.dao;
import module_phong.dao.PhongDAO;
import module_lienket.model.DuLieuLienKet;
import module_khachhang.model.KhachHang;
import module_dichvu.model.*;
import module_datphong.model.DatPhong;
import java.util.*;
import java.util.function.Function;

public class MemoryLienKetDAO implements LienKetDAO {
    private final PhongDAO phongDAO;
    private final module_khachhang.dao.MemoryKhachHangDAO customers;
    private Map<String,DichVu> dvs=new LinkedHashMap<>();
    private Map<String,DatPhong> dps=new LinkedHashMap<>();
    private Map<String,SuDungDichVu> sds=new LinkedHashMap<>();
    public MemoryLienKetDAO(PhongDAO phongDAO,module_khachhang.dao.MemoryKhachHangDAO customers) { this.phongDAO=phongDAO; this.customers=customers; }
    private static <T> void save(Map<String,T> map,String ma,T value,boolean sua) {
        if(map.containsKey(ma)!=sua) throw new IllegalArgumentException(sua?"Không tìm thấy mã.":"Mã đã tồn tại.");
        map.put(ma,value);
    }
    public synchronized <T> T giaoDich(Function<Session,T> work) {
        synchronized(customers) {
        var dv=new LinkedHashMap<>(dvs);
        var dp=new LinkedHashMap<>(dps); var sd=new LinkedHashMap<>(sds);
        try { return phongDAO.giaoDich(ps -> work.apply(new Session() {
            public PhongDAO.Session phongSession() { return ps; }
            public DuLieuLienKet doc() { return new DuLieuLienKet(customers.danhSach(),new ArrayList<>(dvs.values()),new ArrayList<>(dps.values()),new ArrayList<>(sds.values())); }
            public void luuDichVu(DichVu p,boolean sua) { save(dvs,p.maDichVu(),p,sua); }
            public void luuDatPhong(DatPhong p,boolean sua) { save(dps,p.maDatPhong(),p,sua); }
            public void themSuDung(SuDungDichVu p) { save(sds,p.maSuDung(),p,false); }
        })); } catch(RuntimeException e) { dvs=dv; dps=dp; sds=sd; throw e; }
        }
    }
}
