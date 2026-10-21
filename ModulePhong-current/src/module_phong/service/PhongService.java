package module_phong.service;
import module_phong.dao.*;
import module_phong.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import static module_phong.model.TrangThaiPhong.*;

/** Synchronous API. UI must call via SwingWorker; booking uses its own transaction. */
public class PhongService {
    private static final ZoneId HOTEL_ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    private final PhongDAO dao;
    public PhongService(PhongDAO dao) { this.dao=dao; }
    private <T> T tx(Function<PhongDAO.Session,T> fn) { return dao.giaoDich(fn); }
    public DuLieuPhong taiDuLieu() { return tx(s -> new DuLieuPhong(s.phongs(),s.loais(),s.tangs(),s.congViecs())); }
    public List<Phong> getPhongTrong() { return tx(s -> s.phongs().stream().filter(p -> !p.ngungSuDung() && p.trangThai()==TRONG).toList()); }
    public Phong getPhong(String maPhong) { return tx(s -> phong(s,maPhong)); }
    public BigDecimal getGiaHienHanh(String maPhong) { return tx(s -> gia(s,maPhong)); }
    private BigDecimal gia(PhongDAO.Session s,String maPhong) {
        Phong p=phong(s,maPhong); if(p.giaPhong()!=null) return p.giaPhong();
        return s.loais().stream().filter(l -> l.maLoaiPhong().equals(p.maLoaiPhong())).findFirst().orElseThrow().giaTheoNgay();
    }
    public BigDecimal getGiaHienHanh(Connection c,String maPhong) throws SQLException { return gia(sharedSession(c),maPhong); }
    public Phong getPhong(Connection c,String maPhong) throws SQLException { return phong(sharedSession(c),maPhong); }
    // Integration service supplies a DAO session on the SAME transaction.
    public BigDecimal getGiaHienHanh(PhongDAO.Session s,String maPhong) { return gia(s,maPhong); }
    public void giuPhong(PhongDAO.Session s,String maPhong) { chuyen(s,maPhong,TRONG,DA_DAT); }
    public void huyGiuPhong(PhongDAO.Session s,String maPhong) { chuyen(s,maPhong,DA_DAT,TRONG); }
    public void checkIn(PhongDAO.Session s,String maPhong) { chuyen(s,maPhong,DA_DAT,DANG_SU_DUNG); }
    public void checkOut(PhongDAO.Session s,String maPhong) { chuyen(s,maPhong,DANG_SU_DUNG,DANG_DON); }
    private static void require(boolean ok,String text) { if(!ok) throw new IllegalArgumentException(text); }
    private static void code(String c) { require(c!=null && c.matches("[A-Z0-9_-]{1,20}"),"Mã gồm 1–20 ký tự A–Z, 0–9, _ hoặc -."); }
    private static void text(String t,int max,String field) { require(t!=null && !t.isBlank() && t.length()<=max,field+" bắt buộc, tối đa "+max+" ký tự."); }
    private static void price(BigDecimal n) { require(n!=null && n.signum()>0 && n.compareTo(new BigDecimal("9999999999999999.99"))<=0 && n.stripTrailingZeros().scale()<=2,"Giá phải dương, tối đa 2 số thập phân, vừa DECIMAL(18,2)."); }
    private static Phong phong(PhongDAO.Session s,String ma) { return s.phongs().stream().filter(p -> p.maPhong().equals(ma)).findFirst().orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng.")); }
    public void luuPhong(Phong p,boolean sua) { tx(s -> {
        code(p.maPhong()); text(p.soPhong(),20,"Số phòng"); require(p.trangThai()!=null,"Thiếu trạng thái.");
        require(s.loais().stream().anyMatch(l -> l.maLoaiPhong().equals(p.maLoaiPhong())),"Loại phòng không hợp lệ.");
        require(s.tangs().stream().anyMatch(t -> t.maTang().equals(p.maTang())),"Tầng không hợp lệ.");
        if(p.giaPhong()!=null) price(p.giaPhong());
        require(s.phongs().stream().noneMatch(r -> !r.maPhong().equals(p.maPhong()) && r.maTang().equals(p.maTang()) && r.soPhong().equalsIgnoreCase(p.soPhong())),"Số phòng trùng trên cùng tầng.");
        if(sua) {
            Phong old=phong(s,p.maPhong());
            require(old.trangThai()==p.trangThai() && old.ngungSuDung()==p.ngungSuDung(),"Không sửa trạng thái qua form phòng.");
            require(!old.ngungSuDung(),"Phòng đã ngừng sử dụng.");
            require(old.trangThai()==TRONG,"Chỉ sửa thông tin phòng khi phòng trống.");
        } else require(p.trangThai()==TRONG && !p.ngungSuDung(),"Phòng mới phải ở trạng thái TRONG.");
        s.luuPhong(p,sua); return null;
    }); }
    public void luuLoai(LoaiPhong p,boolean sua) { tx(s -> {
        code(p.maLoaiPhong()); text(p.tenLoai(),100,"Tên loại"); price(p.giaTheoNgay());
        require(p.soNguoiToiDa()>0 && p.soNguoiToiDa()<=20,"Sức chứa từ 1–20."); s.luuLoai(p,sua); return null;
    }); }
    public void luuTang(Tang p,boolean sua) { tx(s -> { code(p.maTang()); text(p.tenTang(),100,"Tên tầng"); s.luuTang(p,sua); return null; }); }
    public void xoaLoai(String ma) { tx(s -> {
        require(s.loais().stream().anyMatch(l -> l.maLoaiPhong().equals(ma)),"Không tìm thấy loại phòng.");
        require(s.phongs().stream().noneMatch(p -> p.maLoaiPhong().equals(ma)),"Loại còn phòng tham chiếu, kể cả phòng ngừng sử dụng.");
        s.xoaLoai(ma); return null;
    }); }
    public void xoaTang(String ma) { tx(s -> {
        require(s.tangs().stream().anyMatch(t -> t.maTang().equals(ma)),"Không tìm thấy tầng.");
        require(s.phongs().stream().noneMatch(p -> p.maTang().equals(ma)),"Tầng còn phòng tham chiếu, kể cả phòng ngừng sử dụng.");
        s.xoaTang(ma); return null;
    }); }
    public void ngungSuDung(String ma) { tx(s -> {
        Phong p=phong(s,ma); require(!p.ngungSuDung() && p.trangThai()==TRONG,"Chỉ ngừng sử dụng phòng trống.");
        s.luuPhong(new Phong(p.maPhong(),p.soPhong(),p.maLoaiPhong(),p.maTang(),p.giaPhong(),p.trangThai(),true),true); return null;
    }); }
    private void chuyen(PhongDAO.Session s,String ma,TrangThaiPhong tu,TrangThaiPhong den) {
        Phong p=phong(s,ma); require(!p.ngungSuDung() && p.trangThai()==tu,"Trạng thái đã thay đổi hoặc không phù hợp: cần "+tu.name()+".");
        s.luuPhong(new Phong(p.maPhong(),p.soPhong(),p.maLoaiPhong(),p.maTang(),p.giaPhong(),den,false),true);
    }
    private PhongDAO.Session sharedSession(Connection connection) throws SQLException {
        require(dao instanceof SqlServerPhongDAO,"API transaction Connection chỉ dùng cho SQL Server.");
        require(!connection.getAutoCommit(),"Module đặt phòng phải tắt autoCommit và tự commit/rollback.");
        return ((SqlServerPhongDAO)dao).session(connection);
    }
    private void booking(Connection connection,String ma,TrangThaiPhong tu,TrangThaiPhong den) throws SQLException { chuyen(sharedSession(connection),ma,tu,den); }
    public void giuPhong(Connection c,String maPhong) throws SQLException { booking(c,maPhong,TRONG,DA_DAT); }
    public void huyGiuPhong(Connection c,String maPhong) throws SQLException { booking(c,maPhong,DA_DAT,TRONG); }
    public void checkIn(Connection c,String maPhong) throws SQLException { booking(c,maPhong,DA_DAT,DANG_SU_DUNG); }
    public void checkOut(Connection c,String maPhong) throws SQLException { booking(c,maPhong,DANG_SU_DUNG,DANG_DON); }
    public void batDauCongViec(String loai,String maPhong,String noiDung,String nguoiPhuTrach) { tx(s -> {
        require(loai.equals("VESINH") || loai.equals("BAOTRI"),"Loại công việc không hợp lệ.");
        text(noiDung,500,"Nội dung"); text(nguoiPhuTrach,100,"Người phụ trách");
        Phong p=phong(s,maPhong); require(!p.ngungSuDung(),"Phòng đã ngừng sử dụng.");
        require(s.congViecs().stream().noneMatch(j -> j.maPhong().equals(maPhong) && j.dangThucHien()),"Phòng đang có công việc chưa hoàn tất.");
        if(loai.equals("BAOTRI")) chuyen(s,maPhong,TRONG,BAO_TRI);
        else {
            require(p.trangThai()==TRONG || p.trangThai()==DANG_DON,"Vệ sinh chỉ áp dụng phòng trống/đang dọn.");
            if(p.trangThai()==TRONG) chuyen(s,maPhong,TRONG,DANG_DON);
        }
        s.themCongViec(new CongViecPhong(UUID.randomUUID().toString(),loai,maPhong,noiDung,nguoiPhuTrach,LocalDateTime.now(HOTEL_ZONE),null)); return null;
    }); }
    public void hoanTatCongViec(String maCongViec) { tx(s -> {
        CongViecPhong j=s.congViecs().stream().filter(v -> v.maCongViec().equals(maCongViec)).findFirst().orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc."));
        require(j.dangThucHien(),"Công việc đã hoàn tất.");
        chuyen(s,j.maPhong(),j.loai().equals("VESINH")?DANG_DON:BAO_TRI,TRONG);
        s.ketThucCongViec(new CongViecPhong(j.maCongViec(),j.loai(),j.maPhong(),j.noiDung(),j.nguoiPhuTrach(),j.batDau(),LocalDateTime.now(HOTEL_ZONE))); return null;
    }); }
    public static PhongService demo() {
        return demo(new MemoryPhongDAO());
    }
    public static PhongService demo(MemoryPhongDAO dao) {
        PhongService service=new PhongService(dao);
        service.luuLoai(new LoaiPhong("STD","Standard",2,new BigDecimal("650000")),false);
        service.luuLoai(new LoaiPhong("DLX","Deluxe",3,new BigDecimal("950000")),false);
        service.luuLoai(new LoaiPhong("STE","Suite",4,new BigDecimal("1600000")),false);
        for(int f=1;f<=3;f++) {
            service.luuTang(new Tang("T"+f,"Tầng "+f),false);
            for(int n=1;n<=4;n++) service.luuPhong(new Phong("P"+f+"0"+n,f+"0"+n,n==4?"STE":n==3?"DLX":"STD","T"+f,null,TRONG,false),false);
        }
        service.batDauCongViec("VESINH","P101","Dọn phòng định kỳ","An");
        service.batDauCongViec("BAOTRI","P202","Kiểm tra điều hòa","Bình"); return service;
    }
}
