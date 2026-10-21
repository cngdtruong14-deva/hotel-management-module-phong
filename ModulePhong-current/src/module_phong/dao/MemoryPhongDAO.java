package module_phong.dao;
import module_phong.model.*;
import java.util.*;
import java.util.function.Function;

/** Explicit demo backend. Transactions restore all maps on failure. */
public class MemoryPhongDAO implements PhongDAO, PhongDAO.Session {
    private Map<String,Phong> phongs = new LinkedHashMap<>();
    private Map<String,LoaiPhong> loais = new LinkedHashMap<>();
    private Map<String,Tang> tangs = new LinkedHashMap<>();
    private Map<String,CongViecPhong> jobs = new LinkedHashMap<>();
    public synchronized <T> T giaoDich(Function<Session,T> work) {
        var p = new LinkedHashMap<>(phongs); var l = new LinkedHashMap<>(loais);
        var t = new LinkedHashMap<>(tangs); var j = new LinkedHashMap<>(jobs);
        try { return work.apply(this); }
        catch (RuntimeException e) { phongs=p; loais=l; tangs=t; jobs=j; throw e; }
    }
    public List<Phong> phongs() { return new ArrayList<>(phongs.values()); }
    public List<LoaiPhong> loais() { return new ArrayList<>(loais.values()); }
    public List<Tang> tangs() { return new ArrayList<>(tangs.values()); }
    public List<CongViecPhong> congViecs() { return new ArrayList<>(jobs.values()); }
    private static <T> void save(Map<String,T> map,String ma,T value,boolean sua) {
        if (map.containsKey(ma) != sua) throw new IllegalArgumentException(sua ? "Không tìm thấy bản ghi." : "Mã đã tồn tại.");
        map.put(ma,value);
    }
    public void luuPhong(Phong p,boolean sua) { save(phongs,p.maPhong(),p,sua); }
    public void luuLoai(LoaiPhong p,boolean sua) { save(loais,p.maLoaiPhong(),p,sua); }
    public void luuTang(Tang p,boolean sua) { save(tangs,p.maTang(),p,sua); }
    public void xoaLoai(String ma) { loais.remove(ma); }
    public void xoaTang(String ma) { tangs.remove(ma); }
    public void themCongViec(CongViecPhong c) { save(jobs,c.maCongViec(),c,false); }
    public void ketThucCongViec(CongViecPhong c) { save(jobs,c.maCongViec(),c,true); }
}
