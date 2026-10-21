import common.*;
import module_khachhang.dao.*;
import module_khachhang.model.*;
import module_khachhang.service.*;
import module_lienket.dao.*;
import module_lienket.service.*;
import module_lienket.model.*;
import module_phong.dao.*;
import module_phong.service.*;
import module_phong.model.*;
import module_datphong.model.*;
import module_dichvu.model.*;
import java.math.BigDecimal;
import java.nio.file.Path;

public class SyncTest {
 static void check(boolean b) { if(!b) throw new AssertionError(); }
 static void reject(Runnable r) { try {r.run();} catch(IllegalArgumentException e) {return;} throw new AssertionError("Expected rejection"); }
 public static void main(String[] args) throws Exception {
  PhongService rooms; KhachHangService customers; LienKetDAO dao;
  if(args.length>0) {
   var db=new DBConnection(Path.of(args[0]));
   try(var c=db.open(); var s=c.createStatement(); var r=s.executeQuery("SELECT DB_NAME()")) {
    r.next(); if(!r.getString(1).startsWith("HotelManagement_Sync_Test_")) throw new IllegalStateException("Test DB only");
   }
   var pd=new SqlServerPhongDAO(db); rooms=new PhongService(pd);
   customers=new KhachHangService(new SqlServerKhachHangDAO(db)); dao=new SqlServerLienKetDAO(db,pd);
  } else {
   var pd=new MemoryPhongDAO(); rooms=PhongService.demo(pd); var cd=new MemoryKhachHangDAO();
   customers=new KhachHangService(cd); dao=new MemoryLienKetDAO(pd,cd);
  }
  var links=new LienKetService(dao,rooms);
  var k=customers.themKhachHang(new DuLieuKhachHang("Khách đồng bộ",null,null,null,null,null,null,null,null,null));
  check(links.getKhachHang(k.maKhachHang()).sdt()==null);
  var passport=new DuLieuKhachHang("Alex",LoaiGiayTo.PASSPORT,"AB123456","US","+12025550123",null,null,null,"US",null);
  customers.suaKhachHang(k.maKhachHang(),passport);
  check(links.getKhachHang(k.maKhachHang()).soGiayTo().equals("AB123456"));
  check(links.getKhachHang(k.maKhachHang()).cccd()==null);
  customers.voHieuHoa(k.maKhachHang());
  reject(() -> links.taoDatPhong("DISABLED",k.maKhachHang(),"P103"));
  check(rooms.getPhong("P103").trangThai()==TrangThaiPhong.TRONG);
  customers.kichHoatLai(k.maKhachHang());
  LienKetDAO failing=new LienKetDAO() {
   public <T>T giaoDich(java.util.function.Function<Session,T> work) {return dao.giaoDich(s -> work.apply(new Session() {
    public PhongDAO.Session phongSession(){return s.phongSession();}
    public DuLieuLienKet doc(){return s.doc();}
    public void luuDichVu(DichVu d,boolean u){s.luuDichVu(d,u);}
    public void themSuDung(SuDungDichVu d){s.themSuDung(d);}
    public void luuDatPhong(DatPhong d,boolean u){throw new IllegalStateException("Injected failure");}
   }));}
  };
  try {new LienKetService(failing,rooms).taoDatPhong("ROLLBACK",k.maKhachHang(),"P103"); throw new AssertionError();}
  catch(IllegalStateException expected){}
  check(rooms.getPhong("P103").trangThai()==TrangThaiPhong.TRONG);
  check(links.getDatPhongTheoPhong("P103").isEmpty());
  links.taoDatPhong("SYNC001",k.maKhachHang(),"P103"); links.checkIn("SYNC001");
  links.luuDichVu(new DichVu("SYNC_DV","Dịch vụ",new BigDecimal("20000"),"Lần",false),false);
  links.ghiNhanDichVu("SYNC001","SYNC_DV",2);
  links.luuDichVu(new DichVu("SYNC_DV","Dịch vụ",new BigDecimal("30000"),"Lần",false),true);
  check(links.getTienDichVu("SYNC001").compareTo(new BigDecimal("40000"))==0);
  customers.voHieuHoa(k.maKhachHang());
  check(links.taiDuLieu().khachHangs().stream().anyMatch(p -> p.maKhachHang().equals(k.maKhachHang())));
  check(customers.getKhachHangs().stream().noneMatch(p -> p.maKhachHang().equals(k.maKhachHang())));
  links.checkOut("SYNC001"); check(rooms.getPhong("P103").trangThai()==TrangThaiPhong.DANG_DON);
  check(links.getDatPhongTheoPhong("P103").get(0).maKhachHang().equals(k.maKhachHang()));
  System.out.println("PASS: shared customers, null fields, Passport, edits, inactive booking rejected, rollback, stay/service links, frozen prices, preserved history");
 }
}
