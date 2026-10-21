import common.*;
import module_khachhang.dao.*;
import module_khachhang.model.*;
import module_khachhang.service.*;
import java.nio.file.Path;
public class CustomerUniqueTest {
 static DuLieuKhachHang data(String doc) {return new DuLieuKhachHang("Test customer",LoaiGiayTo.CCCD,doc,"VN",null,null,null,null,null,null);}
 static void reject(Runnable action) {try {action.run();} catch(IllegalArgumentException e) {if(e.getCause() instanceof java.sql.SQLException) return; throw e;} throw new AssertionError("Duplicate document was accepted");}
 public static void main(String[] args)throws Exception {
 var db=new DBConnection(Path.of(args[0]));
 try(var c=db.open();var s=c.createStatement();var rs=s.executeQuery("SELECT DB_NAME()")){rs.next();if(!rs.getString(1).contains("CustomerFix"))throw new IllegalStateException("Test DB only");}
 var svc=new KhachHangService(new SqlServerKhachHangDAO(db));
 var one=svc.themKhachHang(data("999999998001"));
 reject(()->svc.themKhachHang(data("999999998001")));
 var two=svc.themKhachHang(data("999999998002"));
 reject(()->svc.suaKhachHang(two.maKhachHang(),data("999999998001")));
 if(!svc.getKhachHang(two.maKhachHang()).soGiayTo().equals("999999998002"))throw new AssertionError("Failed update changed data");
 svc.suaKhachHang(one.maKhachHang(),data("999999998001"));
 for(int i=0;i<2;i++)svc.themKhachHang(new DuLieuKhachHang("No document",null,null,null,null,null,null,null,null,null));
 for(String country:new String[]{"US","GB"})svc.themKhachHang(new DuLieuKhachHang("Passport",LoaiGiayTo.PASSPORT,"TEST998",country,null,null,null,null,country,null));
 svc.voHieuHoa(one.maKhachHang());svc.kichHoatLai(one.maKhachHang());
 var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
 try {var gate=new java.util.concurrent.CountDownLatch(1); var jobs=new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
 for(int i=0;i<2;i++)jobs.add(pool.submit(()->{gate.await();try{svc.themKhachHang(data("999999998003"));return true;}catch(IllegalArgumentException e){if(e.getCause() instanceof java.sql.SQLException)return false;throw e;}}));
 gate.countDown();int ok=0;for(var job:jobs)if(job.get())ok++;if(ok!=1)throw new AssertionError("Concurrent success count="+ok);
 }finally{pool.shutdownNow();}
 System.out.println("PASS: duplicate insert/update blocked; self-update, null documents, different issuers, status changes and concurrent insert verified through unchanged customer DAO");
 }
}
