package module_phong.model;
import java.time.LocalDateTime;
import java.util.List;

/** Read models and messages. Booking records remain owned by module_datphong. */
public final class HoSoPhong {
 private HoSoPhong() {}
 public record ChiTiet(String maPhong,String moTa) {}
 public record TienNghi(String maPhong,String maTienNghi,String tenTienNghi,int soLuong,String tinhTrang) {}
 public record YeuCau(String maYeuCau,String maKhachHang,String maPhong,String ghiChu,String trangThai,
                      String maDatPhong,String lyDo,long phienBan,LocalDateTime taoLuc,LocalDateTime capNhatLuc) {}
 public record LuuTru(String maDatPhong,String maPhong,String maKhachHang,String trangThai,long phienBan,LocalDateTime capNhatLuc) {public LuuTru{if(capNhatLuc!=null)capNhatLuc=capNhatLuc.withNano(0);}}
 public record DanhGia(String maDanhGia,String maDatPhong,String maPhong,String maKhachHang,int diem,String noiDung,LocalDateTime taoLuc) {public DanhGia{if(taoLuc!=null)taoLuc=taoLuc.withNano(0);}}
 public record PhanAnh(String maPhanAnh,String maDatPhong,String maPhong,String maKhachHang,String noiDung,String trangThai,long phienBan,LocalDateTime capNhatLuc) {public PhanAnh{if(capNhatLuc!=null)capNhatLuc=capNhatLuc.withNano(0);}}
 public record DuLieu(List<ChiTiet> chiTiets,List<TienNghi> tienNghis,List<YeuCau> yeuCaus,List<LuuTru> luuTrus,List<DanhGia> danhGias,List<PhanAnh> phanAnhs) {
  public DuLieu {chiTiets=List.copyOf(chiTiets);tienNghis=List.copyOf(tienNghis);yeuCaus=List.copyOf(yeuCaus);luuTrus=List.copyOf(luuTrus);danhGias=List.copyOf(danhGias);phanAnhs=List.copyOf(phanAnhs);}
  public static DuLieu rong(){return new DuLieu(List.of(),List.of(),List.of(),List.of(),List.of(),List.of());}
 }
}
