import module_phong.model.HoSoPhong.*;
import module_phong.dao.*;
import module_phong.service.*;
import module_khachhang.dao.*;
import module_khachhang.service.*;
import module_khachhang.model.*;
import module_lienket.dao.*;
import module_lienket.service.*;
import java.time.*;
import java.util.*;

public class PortalTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 static void bad(Runnable r){try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("Expected rejection");}
 public static void main(String[] args)throws Exception {
  if(args.length>0){sql(args[0]);return;}
  var pd=new MemoryPhongDAO();var ps=PhongService.demo(pd);var kd=new MemoryKhachHangDAO();var ks=new KhachHangService(kd);
  var kh=ks.themKhachHang(new DuLieuKhachHang("Khách A",null,null,null,null,null,null,null,null,null));
  var kh2=ks.themKhachHang(new DuLieuKhachHang("Khách B",null,null,null,null,null,null,null,null,null));
  var ld=new MemoryLienKetDAO(pd,kd);var ls=new LienKetService(ld,ps);
  HoSoPhongDAO dao=new MemoryHoSoPhongDAO(ps,ks,id->ls.taiDuLieu().datPhongs().stream().filter(p->p.maDatPhong().equals(id)).findFirst().orElse(null));
  exercise(dao,ps,ks,ls,kh.maKhachHang(),kh2.maKhachHang());
  legacyCustomer();
  System.out.println("PASS Portal: ownership, durable contract, idempotency, stale events, verified review/complaint, room details");
 }
 static void sql(String config)throws Exception {
  var db=new common.DBConnection(java.nio.file.Path.of(config));
  try(var c=db.open();var s=c.createStatement();var r=s.executeQuery("SELECT DB_NAME()")){r.next();if(!r.getString(1).startsWith("HotelManagement_Portal_Test_"))throw new IllegalStateException("Isolated test DB only");}
  common.SchemaContract.verify(db);
  var ps=new PhongService(new SqlServerPhongDAO(db));var ks=new KhachHangService(new SqlServerKhachHangDAO(db));
  var kh=ks.themKhachHang(new DuLieuKhachHang("SQL Khách A",null,null,null,null,null,null,null,null,null));
  var kh2=ks.themKhachHang(new DuLieuKhachHang("SQL Khách B",null,null,null,null,null,null,null,null,null));
  var ls=new LienKetService(new SqlServerLienKetDAO(db,new SqlServerPhongDAO(db)),ps);
  exercise(new SqlServerHoSoPhongDAO(db),ps,ks,ls,kh.maKhachHang(),kh2.maKhachHang());
  var reopened=new HoSoPhongService(new SqlServerHoSoPhongDAO(db)).doc();
  ok(reopened.danhGias().size()==1&&reopened.phanAnhs().size()==1&&reopened.yeuCaus().size()==1);
  try(var c=db.open();var s=c.createStatement()){
   c.setAutoCommit(false);
    try{s.executeUpdate("UPDATE dbo.PhongDanhGiaNhan SET MaPhong='NO_SUCH_ROOM' WHERE MaDanhGia='DG01'");throw new AssertionError("Room FK missing");}
   catch(java.sql.SQLException expected){ok(expected.getErrorCode()==547);}finally{c.rollback();}
  }
  System.out.println("PASS SQL Portal: source matching in service, simple FK, rollback, persisted data, request/status/review/complaint round trips");
 }
 static void legacyCustomer(){
  var customer=new DuLieuKhachHang("Khách cũ",null,null,null,null,null,null,null,null,null).thanhHoSo("kh001",LocalDateTime.now(),TrangThaiKhachHang.HOAT_DONG);
  var ks=new KhachHangService(new KhachHangDAO(){
   public java.util.List<KhachHang> danhSach(){return java.util.List.of(customer);}public KhachHang get(String id){if(!id.equals(customer.maKhachHang()))throw new IllegalArgumentException();return customer;}
   public KhachHang them(DuLieuKhachHang d,LocalDateTime t){throw new UnsupportedOperationException();}public void sua(String id,DuLieuKhachHang d){throw new UnsupportedOperationException();}public void doiTrangThai(String id,TrangThaiKhachHang t){throw new UnsupportedOperationException();}
  });
  var rooms=PhongService.demo(new MemoryPhongDAO());
  var stay=new module_datphong.model.DatPhong("legacy-stay","kh001","P104",java.math.BigDecimal.ONE,LocalDateTime.now(),LocalDateTime.now(),LocalDateTime.now(),module_datphong.model.TrangThaiDatPhong.DA_TRA);
  var dao=new MemoryHoSoPhongDAO(rooms,ks,id->id.equals(stay.maDatPhong())?stay:null);
  new HoSoPhongService(dao).guiYeuCau("LEGACYYC","kh001","P104","");
  new TiepNhanLuuTruService(dao).nhanDanhGia(new DanhGia("LEGACYDG",stay.maDatPhong(),"P104","kh001",5,"Dữ liệu cũ hợp lệ",LocalDateTime.now()));
  ok(new HoSoPhongService(dao).doc().danhGias().size()==1);
 }
 static void exercise(HoSoPhongDAO dao,PhongService ps,KhachHangService ks,LienKetService ls,String kh,String kh2){
  var portal=new HoSoPhongService(dao);var inbound=new TiepNhanLuuTruService(dao);
  portal.luuChiTiet(new ChiTiet("P103","Phòng hướng vườn"));
  portal.luuTienNghi(new TienNghi("P103","TV","Smart TV",1,"TOT"));
  ok(new HoSoPhongService(dao).doc().tienNghis().stream().filter(x->x.maPhong().equals("P103")).count()==1);
  bad(()->portal.luuTienNghi(new TienNghi("P103","X","Hỏng",-1,"TOT")));
  portal.guiYeuCau("YC01",kh,"P103","Ưu tiên yên tĩnh");
  portal.guiYeuCau("YC01",kh,"P103","Ưu tiên yên tĩnh");
  ok(portal.doc().yeuCaus().size()==1);
  bad(()->portal.guiYeuCau("YC01",kh2,"P103","Khác"));
  ok(ps.getPhong("P103").trangThai()==module_phong.model.TrangThaiPhong.TRONG);
  ok(ls.getDatPhongTheoPhong("P103").isEmpty());
  ks.voHieuHoa(kh2);bad(()->portal.guiYeuCau("YC02",kh2,"P104",""));ks.kichHoatLai(kh2);
  ok(inbound.yeuCauChoXuLy().size()==1);
  inbound.traKetQua("YC01","TIEP_NHAN",null,"Đang xử lý",1);
  inbound.traKetQua("YC01","CHO_XU_LY",null,"Cũ",0);
  ok(portal.doc().yeuCaus().get(0).trangThai().equals("TIEP_NHAN"));
  bad(()->inbound.traKetQua("YC01","HOAN_TAT","NO_SUCH","",2));
  ls.taoDatPhong("PORTAL_DP",kh,"P103");ls.checkIn("PORTAL_DP");
  inbound.traKetQua("YC01","HOAN_TAT","PORTAL_DP","Đã tiếp nhận",2);
  bad(()->inbound.nhanTrangThai(new LuuTru("PORTAL_DP","P103",kh2,"DANG_O",1,LocalDateTime.now())));
  inbound.nhanTrangThai(new LuuTru("PORTAL_DP","P103",kh,"DANG_O",1,LocalDateTime.now()));
  inbound.nhanPhanAnh(new PhanAnh("PA01","PORTAL_DP","P103",kh,"Điều hòa ồn","MOI",1,LocalDateTime.now()));
  bad(()->inbound.nhanDanhGia(new DanhGia("DG01","PORTAL_DP","P103",kh,5,"Tốt",LocalDateTime.now())));
  ls.checkOut("PORTAL_DP");
  ok(portal.doc().luuTrus().stream().noneMatch(x->x.trangThai().equals("DANG_O")));
  inbound.nhanTrangThai(new LuuTru("PORTAL_DP","P103",kh,"DA_TRA",2,LocalDateTime.now()));
  inbound.nhanTrangThai(new LuuTru("PORTAL_DP","P103",kh,"DANG_O",1,LocalDateTime.now()));
  ok(portal.doc().luuTrus().get(0).trangThai().equals("DA_TRA"));
  inbound.nhanPhanAnh(new PhanAnh("PA01","PORTAL_DP","P103",kh,"Điều hòa ồn","DA_XU_LY",2,LocalDateTime.now()));
  inbound.nhanPhanAnh(new PhanAnh("PA01","PORTAL_DP","P103",kh,"Điều hòa ồn","MOI",1,LocalDateTime.now()));
  ok(portal.doc().phanAnhs().get(0).trangThai().equals("DA_XU_LY"));
  var review=new DanhGia("DG01","PORTAL_DP","P103",kh,4,"Sạch sẽ",LocalDateTime.now());
  inbound.nhanDanhGia(review);inbound.nhanDanhGia(review);
  ok(portal.doc().danhGias().size()==1);
  bad(()->inbound.nhanDanhGia(new DanhGia("DG02","PORTAL_DP","P104",kh,5,"Sai phòng",LocalDateTime.now())));
  bad(()->inbound.nhanDanhGia(new DanhGia("DG03","PORTAL_DP","P103",kh,6,"Sai điểm",LocalDateTime.now())));
  try{dao.giaoDich(s->{s.luu(new ChiTiet("P103","Không được lưu"));throw new IllegalStateException("Injected");});}catch(IllegalStateException expected){}
  ok(portal.doc().chiTiets().stream().filter(x->x.maPhong().equals("P103")).findFirst().orElseThrow().moTa().equals("Phòng hướng vườn"));
 }
}
