package module_phong.dao;
import module_phong.model.HoSoPhong.*;
import module_phong.model.Phong;
import module_khachhang.model.KhachHang;
import module_datphong.model.DatPhong;
import module_phong.service.PhongService;
import module_khachhang.service.KhachHangService;
import java.util.*;
import java.util.function.*;

/** Demo store only. SQL DAO supplies cross-module transaction guarantees. */
public final class MemoryHoSoPhongDAO implements HoSoPhongDAO {
 private final PhongService rooms; private final KhachHangService customers;
 private final Function<String,DatPhong> stays;
 private Map<String,ChiTiet> details=new LinkedHashMap<>(); private Map<String,TienNghi> amenities=new LinkedHashMap<>();
 private Map<String,YeuCau> requests=new LinkedHashMap<>(); private Map<String,LuuTru> status=new LinkedHashMap<>();
 private Map<String,DanhGia> reviews=new LinkedHashMap<>(); private Map<String,PhanAnh> issues=new LinkedHashMap<>();
 public MemoryHoSoPhongDAO(PhongService rooms,KhachHangService customers,Function<String,DatPhong> stays){this.rooms=rooms;this.customers=customers;this.stays=stays;}
 public synchronized <T>T giaoDich(Function<Session,T> work){
  var a=new LinkedHashMap<>(details);var b=new LinkedHashMap<>(amenities);var c=new LinkedHashMap<>(requests);
  var d=new LinkedHashMap<>(status);var e=new LinkedHashMap<>(reviews);var f=new LinkedHashMap<>(issues);
  try{return work.apply(new Session(){
   public KhachHang khach(String ma){return customers.getKhachHang(ma);}
   public Phong phong(String ma){return rooms.getPhong(ma);}
   public DatPhong datPhong(String ma){var p=stays.apply(ma);if(p==null)throw new IllegalArgumentException("Không tìm thấy lượt lưu trú.");return p;}
   public DuLieu doc(){return new DuLieu(new ArrayList<>(details.values()),new ArrayList<>(amenities.values()),new ArrayList<>(requests.values()),new ArrayList<>(status.values()),new ArrayList<>(reviews.values()),new ArrayList<>(issues.values()));}
   public void luu(ChiTiet p){details.put(p.maPhong(),p);} public void luu(TienNghi p){amenities.put(p.maPhong()+"/"+p.maTienNghi(),p);}
   public void luu(YeuCau p){requests.put(p.maYeuCau(),p);} public void luu(LuuTru p){status.put(p.maDatPhong(),p);}
   public void luu(DanhGia p){reviews.put(p.maDanhGia(),p);} public void luu(PhanAnh p){issues.put(p.maPhanAnh(),p);}
  });}catch(RuntimeException|Error ex){details=a;amenities=b;requests=c;status=d;reviews=e;issues=f;throw ex;}
 }
}
