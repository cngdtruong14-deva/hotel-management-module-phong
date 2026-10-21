package module_phong.dao;
import common.ConnectionFactory;
import module_phong.model.HoSoPhong.*;
import module_phong.model.*;
import module_khachhang.model.KhachHang;
import module_khachhang.dao.SqlServerKhachHangDAO;
import module_datphong.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public final class SqlServerHoSoPhongDAO implements HoSoPhongDAO {
 private final ConnectionFactory factory;
 public SqlServerHoSoPhongDAO(ConnectionFactory factory){this.factory=factory;}
 private static RuntimeException fail(SQLException ex){return new IllegalStateException("Không truy cập được hồ sơ phòng; kiểm tra SQL 02_bang_chuc_nang.sql. "+ex.getMessage(),ex);}
 public <T>T giaoDich(Function<Session,T> work){try(var c=factory.open()){
  c.setAutoCommit(false);c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
  try{
   // Every portal transaction uses the same cross-module lock order.
   // Coarse locks are deliberate for this small desktop project; never lock inbox before customers.
   for(String table:List.of("KhachHang","Phong","DatPhong"))try(var st=c.createStatement()){
    st.setQueryTimeout(15);try(var rs=st.executeQuery("SELECT * FROM dbo."+table+" WITH (UPDLOCK,HOLDLOCK)")){while(rs.next()){} }
   }
   T result=work.apply(new Jdbc(c));c.commit();return result;
  }catch(RuntimeException|Error|SQLException ex){try{c.rollback();}catch(SQLException roll){ex.addSuppressed(roll);}throw ex;}
 }catch(SQLException ex){throw fail(ex);}}
 private static final class Jdbc implements Session {
  private final Connection c; Jdbc(Connection c){this.c=c;}
  interface Mapper<T>{T read(ResultSet r)throws SQLException;}
  <T>List<T> query(String sql,Mapper<T> mapper,Object... args){try(var st=c.prepareStatement(sql)){
   st.setQueryTimeout(15);bind(st,args);try(var rs=st.executeQuery()){List<T> rows=new ArrayList<>();while(rs.next())rows.add(mapper.read(rs));return rows;}
  }catch(SQLException e){throw fail(e);}}
  static void bind(PreparedStatement st,Object[] args)throws SQLException{for(int i=0;i<args.length;i++){Object x=args[i];if(x instanceof LocalDateTime t)st.setTimestamp(i+1,Timestamp.valueOf(t));else if(x instanceof String s)st.setNString(i+1,s);else st.setObject(i+1,x);}}
  int write(String sql,Object... args){try(var st=c.prepareStatement(sql)){st.setQueryTimeout(15);bind(st,args);return st.executeUpdate();}catch(SQLException e){throw fail(e);}}
  static LocalDateTime time(ResultSet r,String col)throws SQLException{var t=r.getTimestamp(col);return t==null?null:t.toLocalDateTime();}
  static <T>T one(List<T> rows){if(rows.isEmpty())throw new IllegalArgumentException("Không tìm thấy mã liên kết.");return rows.get(0);}
  public KhachHang khach(String ma){return one(query("SELECT * FROM dbo.KhachHang WHERE MaKhachHang=?",SqlServerKhachHangDAO::read,ma));}
  public Phong phong(String ma){return one(query("SELECT * FROM dbo.Phong WHERE MaPhong=?",r->new Phong(r.getString("MaPhong"),r.getString("SoPhong"),r.getString("MaLoaiPhong"),r.getString("MaTang"),r.getBigDecimal("GiaPhong"),TrangThaiPhong.valueOf(r.getString("TrangThai")),r.getBoolean("NgungSuDung")),ma));}
  public DatPhong datPhong(String ma){return one(query("SELECT * FROM dbo.DatPhong WHERE MaDatPhong=?",r->new DatPhong(r.getString("MaDatPhong"),r.getString("MaKhachHang"),r.getString("MaPhong"),r.getBigDecimal("GiaPhongChot"),time(r,"NgayDat"),time(r,"NgayNhan"),time(r,"NgayTra"),TrangThaiDatPhong.valueOf(r.getString("TrangThai"))),ma));}
  public DuLieu doc(){return new DuLieu(
   query("SELECT * FROM dbo.ChiTietPhong",r->new ChiTiet(r.getString("MaPhong"),r.getString("MoTa"))),
   query("SELECT * FROM dbo.PhongTienNghi ORDER BY MaPhong,MaTienNghi",r->new TienNghi(r.getString("MaPhong"),r.getString("MaTienNghi"),r.getString("TenTienNghi"),r.getInt("SoLuong"),r.getString("TinhTrang"))),
   query("SELECT * FROM dbo.YeuCauDatPhong ORDER BY TaoLuc,MaYeuCau",r->new YeuCau(r.getString("MaYeuCau"),r.getString("MaKhachHang"),r.getString("MaPhong"),r.getString("GhiChu"),r.getString("TrangThai"),r.getString("MaDatPhong"),r.getString("LyDo"),r.getLong("PhienBan"),time(r,"TaoLuc"),time(r,"CapNhatLuc"))),
   query("SELECT * FROM dbo.PhongLuuTruNhan ORDER BY MaDatPhong",r->new LuuTru(r.getString("MaDatPhong"),r.getString("MaPhong"),r.getString("MaKhachHang"),r.getString("TrangThai"),r.getLong("PhienBan"),time(r,"CapNhatLuc"))),
   query("SELECT * FROM dbo.PhongDanhGiaNhan ORDER BY TaoLuc DESC",r->new DanhGia(r.getString("MaDanhGia"),r.getString("MaDatPhong"),r.getString("MaPhong"),r.getString("MaKhachHang"),r.getInt("Diem"),r.getString("NoiDung"),time(r,"TaoLuc"))),
   query("SELECT * FROM dbo.PhongPhanAnhNhan ORDER BY CapNhatLuc DESC",r->new PhanAnh(r.getString("MaPhanAnh"),r.getString("MaDatPhong"),r.getString("MaPhong"),r.getString("MaKhachHang"),r.getString("NoiDung"),r.getString("TrangThai"),r.getLong("PhienBan"),time(r,"CapNhatLuc")))
  );}
  public void luu(ChiTiet p){if(write("UPDATE dbo.ChiTietPhong SET MoTa=? WHERE MaPhong=?",p.moTa(),p.maPhong())==0)write("INSERT dbo.ChiTietPhong(MaPhong,MoTa) VALUES(?,?)",p.maPhong(),p.moTa());}
  public void luu(TienNghi p){if(write("UPDATE dbo.PhongTienNghi SET TenTienNghi=?,SoLuong=?,TinhTrang=? WHERE MaPhong=? AND MaTienNghi=?",p.tenTienNghi(),p.soLuong(),p.tinhTrang(),p.maPhong(),p.maTienNghi())==0)write("INSERT dbo.PhongTienNghi(MaPhong,MaTienNghi,TenTienNghi,SoLuong,TinhTrang) VALUES(?,?,?,?,?)",p.maPhong(),p.maTienNghi(),p.tenTienNghi(),p.soLuong(),p.tinhTrang());}
  public void luu(YeuCau p){if(write("UPDATE dbo.YeuCauDatPhong SET TrangThai=?,MaDatPhong=?,LyDo=?,PhienBan=?,CapNhatLuc=? WHERE MaYeuCau=?",p.trangThai(),p.maDatPhong(),p.lyDo(),p.phienBan(),p.capNhatLuc(),p.maYeuCau())==0)write("INSERT dbo.YeuCauDatPhong(MaYeuCau,MaKhachHang,MaPhong,GhiChu,TrangThai,MaDatPhong,LyDo,PhienBan,TaoLuc,CapNhatLuc) VALUES(?,?,?,?,?,?,?,?,?,?)",p.maYeuCau(),p.maKhachHang(),p.maPhong(),p.ghiChu(),p.trangThai(),p.maDatPhong(),p.lyDo(),p.phienBan(),p.taoLuc(),p.capNhatLuc());}
  public void luu(LuuTru p){if(write("UPDATE dbo.PhongLuuTruNhan SET TrangThai=?,PhienBan=?,CapNhatLuc=? WHERE MaDatPhong=?",p.trangThai(),p.phienBan(),p.capNhatLuc(),p.maDatPhong())==0)write("INSERT dbo.PhongLuuTruNhan(MaDatPhong,MaPhong,MaKhachHang,TrangThai,PhienBan,CapNhatLuc) VALUES(?,?,?,?,?,?)",p.maDatPhong(),p.maPhong(),p.maKhachHang(),p.trangThai(),p.phienBan(),p.capNhatLuc());}
  public void luu(DanhGia p){write("INSERT dbo.PhongDanhGiaNhan(MaDanhGia,MaDatPhong,MaPhong,MaKhachHang,Diem,NoiDung,TaoLuc) VALUES(?,?,?,?,?,?,?)",p.maDanhGia(),p.maDatPhong(),p.maPhong(),p.maKhachHang(),p.diem(),p.noiDung(),p.taoLuc());}
  public void luu(PhanAnh p){if(write("UPDATE dbo.PhongPhanAnhNhan SET NoiDung=?,TrangThai=?,PhienBan=?,CapNhatLuc=? WHERE MaPhanAnh=?",p.noiDung(),p.trangThai(),p.phienBan(),p.capNhatLuc(),p.maPhanAnh())==0)write("INSERT dbo.PhongPhanAnhNhan(MaPhanAnh,MaDatPhong,MaPhong,MaKhachHang,NoiDung,TrangThai,PhienBan,CapNhatLuc) VALUES(?,?,?,?,?,?,?,?)",p.maPhanAnh(),p.maDatPhong(),p.maPhong(),p.maKhachHang(),p.noiDung(),p.trangThai(),p.phienBan(),p.capNhatLuc());}
 }
}
